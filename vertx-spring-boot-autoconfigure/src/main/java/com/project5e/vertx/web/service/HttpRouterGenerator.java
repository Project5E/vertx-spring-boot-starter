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
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.*;
import javax.validation.executable.ExecutableValidator;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class HttpRouterGenerator implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final String OPERATION_RESULT_KEY = "operationResult";

    public static final Pattern PATH_PARAMETERS_PATTERN = Pattern.compile("\\{[.;?*+]*([^{}.;?*+]+)[^}]*}");

    private final Vertx vertx;
    private final ProcessResult processResult;
    private final VertxWebProperties properties;
    private Map<HttpMethod, Set<String>> existsPathMap;

    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

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
                        Route route = generateRoute(router, path, httpMethod);
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
        if (e instanceof InvocationTargetException) {
            InvocationTargetException invocationException = (InvocationTargetException) e;
            e = invocationException.getTargetException();
        }
        Throwable finalE = e;
        RouterAdviceDescriptor advice = processResult.getAdviceDescriptors().stream().filter(adviceDescriptor -> {
            for (Class<? extends Throwable> careThrow : adviceDescriptor.getCareThrows()) {
                if (careThrow.isAssignableFrom(finalE.getClass())) {
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
        Object instance = methodDescriptor.getRouterDescriptor().getInstance();

        // TODO 解析时 就该判断出来并报错
        if (baseMethod.getMethodType() == BaseMethodType.RETURN_RESULT
                && ClassUtil.isAssignable(Future.class, baseMethod.getReturnType().getClass())) {
            throw new ReturnTypeWrongException();
        }

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

            // body 参数验证
            for (Annotation ann : parameter.getAnnotations()) {
                Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
                if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
                    Set<ConstraintViolation<Object>> violations = validator.validate(objects[i]);
                    if (CollectionUtils.isNotEmpty(violations)) {
                        throw new ConstraintViolationException(violations);
                    }
                }
            }
        }
        // 方法参数验证
        if (parameters.length > 0) {
            ExecutableValidator execVal = validator.forExecutables();
            Set<ConstraintViolation<Object>> violations = execVal.validateParameters(instance, method, objects);
            if (CollectionUtils.isNotEmpty(violations)) {
                throw new ConstraintViolationException(violations);
            }
        }

        // Write to the response and end it
        Object invokeResult = method.invoke(instance, objects);
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
                    objects[i] = mapJsonToObject(json, type);
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

    /**
     * 需要面临的问题：
     * 1. 字符串转列表
     * 2. 属性名与字段名的细微差别
     * 3. 用户可能需要对字段做一些微调
     *
     * @param json 传入的json
     * @param type 接收参数的类类型
     * @return 响应从json中解析后的类型实例
     */
    private Object mapJsonToObject(JsonObject json, Class<?> type) {
        // 用户实现了IRequestBodyModel以实现对数据的微调
        JsonObject newJson = null;
        if (Arrays.asList(type.getInterfaces()).contains(IRequestBodyModel.class)) {
            try {
                Method formatJsonMethod = type.getDeclaredMethod("formatJson", JsonObject.class);
                newJson = (JsonObject) formatJsonMethod.invoke(type.newInstance(), json);
            } catch (Exception ignored) {
            }
        }
        if (newJson == null) {
            newJson = json.copy();
        }
        // 字段名转换为驼峰命名
        Map<String, Object> newMap = new HashMap<>();
        newJson.stream().forEachOrdered(entry -> {
            String key = entry.getKey();
            Object value = entry.getValue();

            String[] keySegments = key.split("-");
            if (keySegments.length > 1) {
                for (int i = 1; i < keySegments.length; i++) {
                    keySegments[i] = StringUtils.capitalize(keySegments[i]);
                }
            }
            String newKey = StringUtils.join(keySegments);
            newMap.put(newKey, value);
        });
        newJson = JsonObject.mapFrom(newMap);
        // 字符串转列表
        for (Field field : type.getDeclaredFields()) {
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            Object candidateObject = newJson.getValue(fieldName);
            if (List.class.equals(fieldType) && candidateObject instanceof String) {
                newJson.put(fieldName, ((String) candidateObject).split(","));
            }
        }
        // 移除多余字段
        Set<String> legalFields = Arrays.stream(type.getDeclaredFields()).map(Field::getName).collect(Collectors.toSet());
        Collection<String> extraFields = CollectionUtils.subtract(newJson.fieldNames(), legalFields);
        for (String fieldName : extraFields) {
            newJson.remove(fieldName);
        }
        // 最后直接转换
        return newJson.mapTo(type);
    }

    private void dealRequestParam(RoutingContext ctx, Object[] objects, int i, Parameter parameter, Class<?> type) {
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (requestParam != null) {
            String paramName = requestParam.value().trim();
            if (StringUtils.isBlank(paramName)) {
                paramName = parameter.getName();
            }
            List<String> params = ctx.queryParam(paramName);
            if (Collection.class.isAssignableFrom(type)) {
                // TODO 暂时只支持了 string
                // TODO 如果接受类型是 非LIST 类型可能会有问题
                objects[i] = params;
            } else if (type.isArray()) {
                objects[i] = params.toArray();
            }
            if (type.isEnum()) {
                String param = params.get(0);
                objects[i] = enumConvert(type, param);
            } else {
                if (!params.isEmpty()) {
                    String param = params.get(0);
                    objects[i] = baseTypeConvert(type, param);
                }
            }
        }
    }

    private Enum<?> enumConvert(Class<?> type, String param) {
        Enum<?> resEnum = null;
        for (Object enumConsObj : type.getEnumConstants()) {
            Enum<?> enumCons = (Enum<?>) enumConsObj;
            if (Objects.equals(enumCons.name(), param)) {
                resEnum = enumCons;
            } else {
                try {
                    int ord = Integer.parseInt(param);
                    if (enumCons.ordinal() == ord) {
                        resEnum = enumCons;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return resEnum;
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
        // TODO 对于基本类型 应该能使用 TYPE 来判断会简单些
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

    private Route generateRoute(Router router, String path, HttpMethod method) {
        io.vertx.core.http.HttpMethod vertxMethod = io.vertx.core.http.HttpMethod.valueOf(method.name());

        Matcher pathMather = PATH_PARAMETERS_PATTERN.matcher(path);

        List<String> groupNames = new ArrayList<>();
        while (pathMather.find()) {
            String groupName = pathMather.group();
            groupNames.add(groupName.substring(1, groupName.length() - 1));
        }

        String finalPath = pathMather.replaceAll("([^/]+)");

        if (!groupNames.isEmpty()) {
            return router.routeWithRegex(vertxMethod, finalPath).setRegexGroupsNames(groupNames);
        } else {
            return router.route(vertxMethod, finalPath);
        }
    }

}
