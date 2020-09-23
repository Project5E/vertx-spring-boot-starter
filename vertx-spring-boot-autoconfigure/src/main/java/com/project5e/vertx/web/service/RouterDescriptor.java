package com.project5e.vertx.web.service;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class RouterDescriptor {

    @NonNull
    private Class<?> clazz;
    @NonNull
    private Object instance;

    private List<MethodDescriptor> methodDescriptors = new ArrayList<>();

    public void addMethodDescriptor(MethodDescriptor methodDescriptor){
        methodDescriptors.add(methodDescriptor);
    }

}
