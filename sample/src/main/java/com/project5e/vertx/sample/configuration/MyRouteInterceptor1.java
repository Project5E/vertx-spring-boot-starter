package com.project5e.vertx.sample.configuration;

import com.project5e.vertx.web.intercepter.HandlerMethod;
import com.project5e.vertx.web.intercepter.RouteInterceptor;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class MyRouteInterceptor1 implements RouteInterceptor, Ordered {

    @Override
    public boolean matches(Route route) {
        return true;
    }

    @Override
    public void preHandle(RoutingContext context, HandlerMethod handlerMethod) {
        System.out.println("编号为5的前置处理");
        System.out.println(handlerMethod);
        context.next();
    }

    @Override
    public void postHandle(RoutingContext context, HandlerMethod handlerMethod) {
        System.out.println("编号为5的后置处理");
        context.next();
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
