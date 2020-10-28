package com.project5e.vertx.sample.configuration;

import com.project5e.vertx.web.intercepter.HandlerMethod;
import com.project5e.vertx.web.intercepter.RouteInterceptor;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class MyRouteInterceptor2 implements RouteInterceptor {

    @Override
    public boolean matches(Route route) {
        return true;
    }

    @Override
    public void preHandle(RoutingContext context, HandlerMethod handlerMethod) {
        System.out.println("编号为4的前置处理");
        context.next();
    }

    @Override
    public void postHandle(RoutingContext context, HandlerMethod handlerMethod) {
        System.out.println("编号为4的后置处理");
        context.next();
    }
}
