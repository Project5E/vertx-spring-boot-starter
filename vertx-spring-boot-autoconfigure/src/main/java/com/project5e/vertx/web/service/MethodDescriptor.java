package com.project5e.vertx.web.service;

import com.project5e.vertx.web.annotation.HttpMethod;
import com.project5e.vertx.web.component.BaseMethod;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

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
