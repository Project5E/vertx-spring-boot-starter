package com.project5e.vertx.web.service;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.http.ContentType;
import com.project5e.vertx.web.annotation.*;
import com.project5e.vertx.web.autoconfigure.VertxWebProperties;
import com.project5e.vertx.web.component.BaseMethod;
import com.project5e.vertx.web.component.BaseMethodType;
import com.project5e.vertx.web.component.ResponseEntity;
import com.project5e.vertx.web.exception.MappingDuplicateException;
import com.project5e.vertx.web.exception.ReturnTypeWrongException;
import com.project5e.vertx.web.intercepter.HandlerMethod;
import com.project5e.vertx.web.intercepter.RouteInterceptor;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
                        Route route = router.route(io.vertx.core.http.HttpMethod.valueOf(httpMethod.name()), fixPath(path));
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
                methodDescriptor.getRouterDescriptor().getClazz(),
                methodDescriptor.getRouterDescriptor().getInstance(),
                methodDescriptor.getBaseMethod()
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
            BaseMethod baseMethod = advice.getBaseMethod();
            Parameter[] parameters = baseMethod.getParameters();
            Object[] objects = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (Throwable.class.isAssignableFrom(parameter.getType())) {
                    objects[i] = e;
                } else if (RoutingContext.class.isAssignableFrom(parameter.getType())) {
                    objects[i] = ctx;
                }
            }
            Future<?> result = (Future<?>) baseMethod.getMethod().invoke(advice.getInstance(), objects);
            dealResponse(ctx, baseMethod, result);
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
        BaseMethod baseMethod = methodDescriptor.getBaseMethod();
        Method method = baseMethod.getMethod();
        Parameter[] parameters = baseMethod.getParameters();
        Object[] objects = new Object[parameters.length];
        Promise<Object> promiseResult = null;
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();
            dealRequestHeader(ctx, objects, i, parameter, type);
            dealRequestParam(ctx, objects, i, parameter, type);
            dealPathVariable(ctx, objects, i, parameter, type);
            dealRequestBody(ctx, objects, i, parameter, type);
            // 处理没有被注解的参数
            if (objects[i] == null) {
                if (Promise.class.isAssignableFrom(parameter.getType())) {
                    if (baseMethod.getMethodType() == BaseMethodType.PARAM_RESULT) {
                        objects[i] = promiseResult = Promise.promise();
                    }
                } else if (RoutingContext.class.isAssignableFrom(parameter.getType())) {
                    objects[i] = ctx;
                }
            }
        }

        if (baseMethod.getMethodType() == BaseMethodType.RETURN_RESULT
                && ClassUtil.isAssignable(Future.class, baseMethod.getReturnType().getClass())) {
            throw new ReturnTypeWrongException();
        }
        // Write to the response and end it
        Object invokeResult = method.invoke(methodDescriptor.getRouterDescriptor().getInstance(), objects);
        Future<?> returnResult = null;
        switch (baseMethod.getMethodType()) {
            case RETURN_RESULT:
                returnResult = (Future<?>) invokeResult;
                break;
            case PARAM_RESULT:
                returnResult = promiseResult.future();
                break;
        }
        dealResponse(ctx, baseMethod, returnResult);
    }

    private void dealRequestHeader(RoutingContext ctx, Object[] objects, int i, Parameter parameter, Class<?> type) {
        RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);
        if (requestHeader != null) {
            String key = requestHeader.value();
            String headerValue = ctx.request().getHeader(key);
            objects[i] = baseTypeConvert(type, headerValue);
        }
    }

    /**
     * RequestBody 能承接 url 参数
     */
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
                // json
                JsonObject json = ctx.getBodyAsJson();
                if (json == null) {
                    json = getQueryJson(ctx);
                }
                if (type == JsonObject.class) {
                    objects[i] = json;
                } else {
                    objects[i] = json.mapTo(type);
                }
            }
        }
    }

    private JsonObject getQueryJson(RoutingContext ctx) {
        JsonObject object = new JsonObject();
        for (String name : ctx.queryParams().names()) {
            List<String> values = ctx.queryParams().getAll(name);
            if (CollectionUtils.isEmpty(values)) {
                object.putNull(name);
            } else if (values.size() == 1) {
                object.put(name, values.get(0));
            } else {
                object.put(name, values);
            }
        }
        return object;
    }

    private void dealRequestParam(RoutingContext ctx, Object[] objects, int i, Parameter parameter, Class<?> type) {
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (requestParam != null) {
            String value = requestParam.value().trim();
            if (StringUtils.isBlank(value)) {
                value = parameter.getName();
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
                value = parameter.getName();
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

    private void dealResponse(RoutingContext ctx, BaseMethod baseMethod, Future<?> future) {
        HttpServerResponse response = ctx.response();
        if (response.ended()) {
            return;
        }
        future.onComplete(ar -> {
            Object result = ar.result();
            OperationResult operationResult = new OperationResult();
            operationResult.setSucceed(ar.succeeded());
            operationResult.setCause(ar.cause());
            ResponseEntity<?> entity;
            if (baseMethod.isWrapped() && result != null) {
                entity = (ResponseEntity<?>) result;
            } else {
                Class<?> clz = TypeUtil.getClass(baseMethod.getActualType());
                if (clz == null || clz.isPrimitive() || ClassUtil.isPrimitiveWrapper(clz) || CharSequence.class.isAssignableFrom(clz)) {
                    entity = ResponseEntity.completeWithPlainText(result == null ? null : StrUtil.toString(result));
                } else {
                    entity = ResponseEntity.completeWithJson(result);
                }
            }
            operationResult.setResponseEntity(entity);
            ctx.put(OPERATION_RESULT_KEY, operationResult);
            ctx.next();
        });
    }

    private Handler<RoutingContext> completeHandler() {
        return ctx -> {
            OperationResult operationResult = ctx.get(OPERATION_RESULT_KEY);
            if (operationResult.getSucceed()) {
                HttpServerResponse response = ctx.response();
                ResponseEntity<?> entity = operationResult.getResponseEntity();
                if (entity == null) {
                    response.end();
                } else {
                    response.setStatusCode(entity.getStatus());
                    if (StringUtils.isNotBlank(entity.getStatusMessage())) {
                        response.setStatusMessage(entity.getStatusMessage());
                    }
                    if (entity.getHeaders() != null) {
                        response.headers().addAll(entity.getHeaders());
                    }
                    Object payload = entity.getPayload();
                    if (payload == null) {
                        response.end();
                    } else {
                        if (entity.getContentType() == ContentType.JSON) {
                            response.end(Json.encode(payload));
                        } else {
                            response.end((String) payload);
                        }
                    }
                }
            } else {
                ctx.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), operationResult.getCause());
            }
        };
    }

    private String fixPath(String path) {
        return path.replaceAll("\\{", ":").replaceAll("}", "");
    }

}
