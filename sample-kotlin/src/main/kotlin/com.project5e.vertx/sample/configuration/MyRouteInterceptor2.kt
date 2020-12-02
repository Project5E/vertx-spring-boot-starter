package com.project5e.vertx.sample.configuration

import com.project5e.vertx.web.intercepter.HandlerMethod
import com.project5e.vertx.web.intercepter.RouteInterceptor
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(4)
class MyRouteInterceptor2 : RouteInterceptor {
    override fun matches(route: Route): Boolean {
        return true
    }

    override fun preHandle(context: RoutingContext, handlerMethod: HandlerMethod) {
        println("编号为4的前置处理")
        context.next()
    }

    override fun postHandle(context: RoutingContext, handlerMethod: HandlerMethod) {
        println("编号为4的后置处理")
        context.next()
    }
}