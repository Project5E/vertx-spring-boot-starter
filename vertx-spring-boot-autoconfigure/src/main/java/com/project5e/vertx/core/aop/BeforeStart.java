package com.project5e.vertx.core.aop;

import io.vertx.core.Verticle;

/**
 * 在 vertcile start 方法之前被调用
 */
public interface BeforeStart {

    void doBeforeStart(Verticle verticle);

}
