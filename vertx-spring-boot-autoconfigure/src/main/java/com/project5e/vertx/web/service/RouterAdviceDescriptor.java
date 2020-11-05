package com.project5e.vertx.web.service;

import com.project5e.vertx.web.component.BaseMethod;
import lombok.Data;

import java.lang.reflect.Method;

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
