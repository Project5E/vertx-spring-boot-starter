package com.project5e.vertx.swagger.service.proxy;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

@Slf4j
public class JacksonModelResolverProxy implements InvocationHandler {

    private final static String MODEL_CONVERTER_METHOD = "resolve";

    // 真实的解析器
    private final Object delegate;

    public JacksonModelResolverProxy() {
        this.delegate = new ModelResolver(Json.mapper());
    }

    // 虚假的解析器
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals(MODEL_CONVERTER_METHOD)) {
            Type type = ((AnnotatedType) args[0]).getType();
            log.debug("Jackson ModelResolver invoked start, method: {}, type: {}", method.getName(), type.getTypeName());

            Type rawClazz;
            if (type instanceof JavaType) {
                rawClazz = ((JavaType) type).getRawClass();
            } else {
                rawClazz = type;
            }

            Object schema;
            if (rawClazz == JsonObject.class) {
                schema = new ObjectSchema().description("Json对象，内部结构未知");
            } else if (rawClazz == JsonArray.class) {
                schema = new ArraySchema().description("数组，内部结构未知");
            } else if (rawClazz == Void.class) {
                schema = null;
            } else {
                schema = method.invoke(delegate, args);
            }
            log.debug("Jackson ModelResolver invoked end!!, method: {}, type: {}", method.getName(), type);
            log.debug("Jackson ModelResolver invoked end!!, result: {}", schema);
            return schema;
        } else {
            return method.invoke(delegate, args);
        }
    }
}
