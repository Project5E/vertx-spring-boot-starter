package com.project5e.vertx.web.service;

import com.project5e.vertx.web.annotation.HttpMethod;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Data
@RequiredArgsConstructor
public class MethodDescriptor {

    @NonNull
    private RouterDescriptor routerDescriptor;

    // get from annotation
    private String[] paths;
    private HttpMethod[] httpMethods;

    // get from reflect
    private Method method;
    private Parameter[] parameters;
    private Type returnType; // Future<T>
    private Type actualType; // T

    public void setMethod(Method method) {
        this.method = method;
        this.parameters = method.getParameters();
        this.returnType = method.getGenericReturnType();
        Type[] actualTypeArguments = ((ParameterizedType) this.returnType).getActualTypeArguments();
        if (actualTypeArguments.length > 0) {
            actualType = actualTypeArguments[0];
        }
    }

}
