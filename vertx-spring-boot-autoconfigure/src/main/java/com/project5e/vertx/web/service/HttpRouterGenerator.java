package com.project5e.vertx.web.service;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.http.ContentType;
import com.project5e.vertx.web.annotation.*;
import com.project5e.vertx.web.autoconfigure.VertxWebProperties;
import com.project5e.vertx.web.exception.AnnotationEmptyValueException;
import com.project5e.vertx.web.exception.MappingDuplicateException;
import com.project5e.vertx.web.exception.ReturnTypeWrongException;
import com.project5e.vertx.web.intercepter.HandlerMethod;
import com.project5e.vertx.web.intercepter.RouteInterceptor;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class HttpRouterGenerator implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final String OPERATION_RESULT_KEY = "operationResult";

    private final Vertx vertx;
    private final ProcessResult processResult;
    private final VertxWebProperties properties;
    private Map<HttpMethod, Set<String>> existsPathMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public HttpRouterGenerator(Vertx vertx, ProcessResult processResult, VertxWebProperties properties) {
        this.vertx = vertx;
        this.processResult = processResult;
        this.properties = properties;
    }

    public Router generate() {
        existsPathMap = new HashMap<>();
        for (HttpMethod method : HttpMethod.values()) {
            existsPathMap.put(method, new HashSet<>());
        }
        return findAndGenerateRouter();
    }

    private Router findAndGenerateRouter() {
        Router router = Router.router(vertx);
        Route globalRoute = router.route();
        // Cors
        CorsHandler corsHandler = null;
        try {
            corsHandler = applicationContext.getBean(CorsHandler.class);
        } catch (Exception e) {
            log.warn("notfound any CorsHandler");
        }
        if (corsHandler != null) {
            globalRoute.handler(corsHandler);
            log.info("web add CorsHandler");
        }
        for (RouterDescriptor routerDescriptor : processResult.getRouterDescriptors()) {
            for (MethodDescriptor methodDescriptor : routerDescriptor.getMethodDescriptors()) {
                for (HttpMethod httpMethod : methodDescriptor.getHttpMethods()) {
                    for (String path : methodDescriptor.getPaths()) {
                        Route route = router.route(io.vertx.core.http.HttpMethod.valueOf(httpMethod.name()), path);
                        Handler<RoutingContext> businessHandler = ctx -> {
                            try {
                                handleRequest(methodDescriptor, ctx);
                            } catch (Throwable e) {
                                dealError(ctx, e);
                            }
                        };
                        fillHandlers(route, methodDescriptor, businessHandler);
                        Set<String> pathSet = existsPathMap.get(httpMethod);
                        if (pathSet.contains(path)) {
                            throw new MappingDuplicateException(httpMethod, path);
                        }
                        pathSet.add(path);
                        log.debug("HTTP Mapping {} {}", httpMethod.name(), path);
                    }
                }
            }
        }
        return router;
    }

    private void fillHandlers(Route route, MethodDescriptor methodDescriptor, Handler<RoutingContext> handler) {
        List<RouteInterceptor> interceptors = processResult.getRouteInterceptors().stream()
                .filter(routeInterceptor -> routeInterceptor.matches(route))
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .collect(Collectors.toList());
        HandlerMethod handlerMethod = new HandlerMethod(
                methodDescriptor.getMethod(),
                methodDescriptor.getRouterDescriptor().getClazz(),
                methodDescriptor.getRouterDescriptor().getInstance()
        );

        // body处理器
        BodyHandler bodyHandler = BodyHandler.create();
        if (properties.getBodyLimit() != null) {
            bodyHandler.setBodyLimit(properties.getBodyLimit().toBytes());
        }
        route.handler(bodyHandler);
        // 前置处理器
        interceptors.forEach(interceptor -> route.handler(ctx -> interceptor.preHandle(ctx, handlerMethod)));
        // 业务处理器
        route.handler(handler);
        Collections.reverse(interceptors);
        // 后置处理器
        interceptors.forEach(interceptor -> route.handler(ctx -> interceptor.postHandle(ctx, handlerMethod)));
        // 结果处理器
        route.handler(completeHandler());
        // 失败处理器
        route.failureHandler(ctx -> dealError(ctx, ctx.failure()));
    }

    @SneakyThrows
    private void dealError(RoutingContext ctx, Throwable e) {
        RouterAdviceDescriptor advice = processResult.getAdviceDescriptors().stream().filter(adviceDescriptor -> {
            for (Class<? extends Throwable> careThrow : adviceDescriptor.getCareThrows()) {
                if (careThrow.isAssignableFrom(e.getClass())) {
                    return true;
                }
            }
            return false;
        }).findFirst().orElse(null);
        if (advice != null) {
            Parameter[] parameters = advice.getParameters();
            Object[] objects = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (Throwable.class.isAssignableFrom(parameter.getType())) {
                    objects[i] = e;
                } else if (RoutingContext.class.isAssignableFrom(parameter.getType())) {
                    objects[i] = ctx;
                }
            }
            Future<?> result = (Future<?>) advice.getMethod().invoke(advice.getInstance(), objects);
            dealResponse(ctx, advice.getActualType(), result);
            return;
        }
        e.printStackTrace();
        OperationResult operationResult = new OperationResult();
        operationResult.setSucceed(false);
        operationResult.setCause(e);
        ctx.put(OPERATION_RESULT_KEY, operationResult);
        ctx.next();
    }

    private void handleRequest(MethodDescriptor methodDescriptor, RoutingContext ctx) throws Exception {
        Method method = methodDescriptor.getMethod();
        Parameter[] parameters = methodDescriptor.getParameters();
        Object[] objects = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();
            dealRequestHeader(ctx, objects, i, parameter, type);
            dealRequestParam(ctx, objects, i, parameter, type);
            dealPathVariable(ctx, objects, i, parameter, type);
            dealRequestBody(ctx, objects, i, parameter, type);
            // 处理没有被注解的参数
            if (objects[i] == null) {
                if (RoutingContext.class.isAssignableFrom(parameter.getType())) {
                    objects[i] = ctx;
                }
            }
        }

        if (ClassUtil.isAssignable(Future.class, methodDescriptor.getReturnType().getClass())) {
            throw new ReturnTypeWrongException();
        }
        // Write to the response and end it
        Future<?> result = (Future<?>) method.invoke(methodDescriptor.getRouterDescriptor().getInstance(), objects);
        dealResponse(ctx, methodDescriptor.getActualType(), result);
    }

    private void dealRequestHeader(RoutingContext ctx, Object[] objects, int i, Parameter parameter, Class<?> type) {
        RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);
        if (requestHeader != null) {
            String key = requestHeader.value();
            String headerValue = ctx.request().getHeader(key);
            objects[i] = baseTypeConvert(type, headerValue);
        }
    }

    private void dealRequestBody(RoutingContext ctx, Object[] objects, int i, Parameter parameter, Class<?> type) {
        RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
        if (requestBody != null) {
            if (Collection.class.isAssignableFrom(type)) {
                JsonArray bodyAsJsonArray = ctx.getBodyAsJsonArray();
                objects[i] = bodyAsJsonArray.getList();
            } else if (type.isArray()) {
                JsonArray bodyAsJsonArray = ctx.getBodyAsJsonArray();
                objects[i] = bodyAsJsonArray.getList().toArray();
            } else {
                Object o = ctx.getBodyAsJson().mapTo(type);
                objects[i] = o;
            }
        }
    }

    private void dealRequestParam(RoutingContext ctx, Object[] objects, int i, Parameter parameter, Class<?> type) {
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (requestParam != null) {
            String value = requestParam.value().trim();
            if (StringUtils.isBlank(value)) {
                throw new AnnotationEmptyValueException();
            }
            List<String> params = ctx.queryParam(value);
            if (Collection.class.isAssignableFrom(type)) {
                // 暂时只支持了 string
                objects[i] = params;
            } else if (type.isArray()) {
                objects[i] = params.toArray();
            } else {
                if (!params.isEmpty()) {
                    String param = params.get(0);
                    objects[i] = baseTypeConvert(type, param);
                }
            }
        }
    }

    private void dealPathVariable(RoutingContext ctx, Object[] objects, int i, Parameter parameter, Class<?> type) {
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        if (pathVariable != null) {
            String value = pathVariable.value().trim();
            if (StringUtils.isBlank(value)) {
                throw new AnnotationEmptyValueException();
            }
            String param = ctx.pathParam(value);
            objects[i] = baseTypeConvert(type, param);
        }
    }

    private Object baseTypeConvert(Class<?> type, String param) {
        if (type == byte.class || type == Byte.class) {
            return Byte.valueOf(param);
        } else if (type == short.class || type == Short.class) {
            return Integer.valueOf(param);
        } else if (type == int.class || type == Integer.class) {
            return Integer.valueOf(param);
        } else if (type == long.class || type == Long.class) {
            return Long.valueOf(param);
        } else if (type == float.class || type == Float.class) {
            return Float.valueOf(param);
        } else if (type == double.class || type == Double.class) {
            return Double.valueOf(param);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(param);
        } else if (type == char.class || type == Character.class) {
            if (param.length() > 0) {
                return param.charAt(0);
            }
        } else if (type == String.class) {
            return param;
        }
        return null;
    }

    private void dealResponse(RoutingContext ctx, Type type, Future<?> result) {
        HttpServerResponse response = ctx.response();
        if (response.ended()) {
            return;
        }
        ContentType contentType;
        Class<?> clz = TypeUtil.getClass(type);
        if (clz == null || clz.isPrimitive() || ClassUtil.isPrimitiveWrapper(clz) || CharSequence.class.isAssignableFrom(clz)) {
            contentType = ContentType.TEXT_PLAIN;
        } else {
            contentType = ContentType.JSON;
        }
        result.onComplete(ar -> {
            OperationResult operationResult = new OperationResult();
            operationResult.setSucceed(ar.succeeded());
            operationResult.setCause(ar.cause());
            operationResult.setContentType(contentType);
            if (ar.succeeded()) {
                switch (contentType) {
                    case JSON:
                        operationResult.setChunk(Json.encode(ar.result()));
                        break;
                    case TEXT_PLAIN:
                        operationResult.setChunk(StrUtil.toString(ar.result()));
                        break;
                    default:
                        operationResult.setChunk(null);
                }
            }
            ctx.put(OPERATION_RESULT_KEY, operationResult);
            ctx.next();
        });
    }

    private Handler<RoutingContext> completeHandler() {
        return ctx -> {
            OperationResult operationResult = ctx.get(OPERATION_RESULT_KEY);
            if (operationResult.getSucceed()) {
                ctx.response().putHeader("content-type", operationResult.getContentType().getValue());
                ctx.response().end(operationResult.getChunk());
            } else {
                ctx.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), operationResult.getCause());
            }
        };
    }

}
