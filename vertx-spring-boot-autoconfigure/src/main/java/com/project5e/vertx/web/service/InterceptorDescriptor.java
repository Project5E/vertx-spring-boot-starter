package com.project5e.vertx.web.service;

import com.project5e.vertx.web.intercepter.HandlerMethod;
import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@Data
@RequiredArgsConstructor
public class InterceptorDescriptor {

    private int order;
    private Function<Route, Boolean> matchCondition;
    private Function<HandlerMethod, Handler<RoutingContext>> preHandle;
    private Function<HandlerMethod, Handler<RoutingContext>> postHandle;

}
