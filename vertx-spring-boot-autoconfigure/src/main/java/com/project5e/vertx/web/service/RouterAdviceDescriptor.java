package com.project5e.vertx.web.service;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Data
public class RouterAdviceDescriptor {

    private Class<? extends Throwable>[] careThrows;

    private Object instance;

    private BaseMethod baseMethod;


    public RouterAdviceDescriptor(Class<? extends Throwable>[] careThrows, Object instance, Method method) {
        this.careThrows = careThrows;
        this.instance = instance;
        this.baseMethod = new BaseMethod(method);
    }

}
