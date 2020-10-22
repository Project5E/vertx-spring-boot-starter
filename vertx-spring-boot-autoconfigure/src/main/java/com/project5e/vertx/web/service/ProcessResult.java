package com.project5e.vertx.web.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProcessResult {

    private List<RouterDescriptor> routerDescriptors = new ArrayList<>();

    private List<RouterAdviceDescriptor> adviceDescriptors = new ArrayList<>();

    public void addRouterDescriptor(RouterDescriptor routerDescriptor) {
        routerDescriptors.add(routerDescriptor);
    }

    public void addAdviceDescriptor(RouterAdviceDescriptor adviceDescriptor) {
        adviceDescriptors.add(adviceDescriptor);
    }

}
