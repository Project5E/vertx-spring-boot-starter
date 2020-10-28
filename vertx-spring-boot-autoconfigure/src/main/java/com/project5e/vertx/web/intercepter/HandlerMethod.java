package com.project5e.vertx.web.intercepter;

import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Data
public class HandlerMethod {

    private Class<?> routerClass;
    private Object routerInstance;

    // get from reflect
    private Method method;
    private Parameter[] parameters;
    private Type returnType; // Future<T>
    private Type actualType; // T

    public HandlerMethod(Method method, Class<?> routerClass, Object routerInstance) {
        this.routerClass = routerClass;
        this.routerInstance = routerInstance;
        this.method = method;
        this.parameters = method.getParameters();
        this.returnType = method.getGenericReturnType();
        Type[] actualTypeArguments = ((ParameterizedType) this.returnType).getActualTypeArguments();
        if (actualTypeArguments.length > 0) {
            actualType = actualTypeArguments[0];
        }
    }

}
