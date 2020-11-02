package com.project5e.vertx.web.service;

import com.project5e.vertx.web.annotation.HttpMethod;
import io.vertx.core.Promise;
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

    private BaseMethod baseMethod;

    public void setMethod(Method method) {
        baseMethod = new BaseMethod(method);
    }

}
