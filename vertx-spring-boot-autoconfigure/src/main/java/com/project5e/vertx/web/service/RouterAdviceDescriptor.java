package com.project5e.vertx.web.service;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class RouterAdviceDescriptor {

    @NonNull
    private Class<? extends Throwable>[] careThrows;

    @NonNull
    private Object instance;

    private Method method;
    private Parameter[] parameters;
    private Type returnType; // Future<T>
    private Type actualType; // T

    public RouterAdviceDescriptor setMethod(Method method) {
        this.method = method;
        this.parameters = method.getParameters();
        this.returnType = method.getGenericReturnType();
        Type[] actualTypeArguments = ((ParameterizedType) this.returnType).getActualTypeArguments();
        if (actualTypeArguments.length > 0) {
            actualType = actualTypeArguments[0];
        }
        return this;
    }

}
