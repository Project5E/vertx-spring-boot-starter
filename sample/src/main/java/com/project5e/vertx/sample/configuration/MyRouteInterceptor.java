package com.project5e.vertx.sample.configuration;

import com.project5e.vertx.web.intercepter.HandlerMethod;
import com.project5e.vertx.web.intercepter.RouteInterceptor;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRouteInterceptor {

    @Bean
    public RouteInterceptor routeInterceptor1() {
        return new RouteInterceptor() {
            @Override
            public boolean matches(Route route) {
                return true;
            }

            @Override
            public int order() {
                return 0;
            }

            @Override
            public void preHandle(RoutingContext context, HandlerMethod handlerMethod) {
                System.out.println("我在前面，最前");
                context.next();
            }

            @Override
            public void postHandle(RoutingContext context, HandlerMethod handlerMethod) {
                System.out.println("我在后面，最后");
                context.next();
            }
        };
    }

    @Bean
    public RouteInterceptor routeInterceptor2() {
        return new RouteInterceptor() {
            @Override
            public boolean matches(Route route) {
                return true;
            }

            @Override
            public int order() {
                return 1;
            }

            @Override
            public void preHandle(RoutingContext context, HandlerMethod handlerMethod) {
                System.out.println("我在前面，次前");
                context.next();
            }

            @Override
            public void postHandle(RoutingContext context, HandlerMethod handlerMethod) {
                System.out.println("我在后面，次后");
                context.next();
            }
        };
    }

}
