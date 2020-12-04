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

    // 真实的解析器
    private final Object delegate;

    public JacksonModelResolverProxy() {
        this.delegate = new ModelResolver(Json.mapper());
    }

    // 虚假的解析器
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 只对resolve做处理，其它方法直接略过
        if (method.getName().equals("resolve")) {
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
                schema = new ObjectSchema().description("我只能告诉你，这是一个对象，但对象内部结构未知");
            } else if (rawClazz == JsonArray.class) {
                schema = new ArraySchema().description("我只能告诉你，这是一个数组，但数组内部结构未知");
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
