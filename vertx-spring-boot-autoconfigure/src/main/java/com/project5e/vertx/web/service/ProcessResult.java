package com.project5e.vertx.web.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProcessResult {

    private List<RouterDescriptor> routerDescriptors = new ArrayList<>();

    public void addRouterDescriptor(RouterDescriptor routerDescriptor) {
        routerDescriptors.add(routerDescriptor);
    }
}
