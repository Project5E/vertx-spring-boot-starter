package com.project5e.vertx.web.intercepter;

import com.project5e.vertx.web.service.BaseMethod;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HandlerMethod {

    private Class<?> routerClass;

    private Object routerInstance;

    private BaseMethod baseMethod;

}
