package com.project5e.vertx.web.intercepter;

import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;

/**
 * 针对Route的拦截器，在匹配的Route前和Route后加上处理器
 */
public interface RouteInterceptor {

    /**
     * Route是否匹配
     *
     * @param route 目标route
     * @return 返回匹配结果
     */
    boolean matches(Route route);

    /**
     * 顺序，越小优先级越高，如果顺序相同，则先后顺序不予保证
     *
     * @return 顺序
     */
    int order();

    /**
     * Route的前置handler
     * 用户必须调用context.next()才会执行下一步
     */
    void preHandle(RoutingContext context, HandlerMethod handlerMethod);

    /**
     * Route的后置handler
     * 用户必须调用context.next()才会执行下一步
     */
    void postHandle(RoutingContext context, HandlerMethod handlerMethod);

}
