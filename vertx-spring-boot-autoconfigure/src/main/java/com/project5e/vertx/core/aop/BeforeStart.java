package com.project5e.vertx.core.aop;

import io.vertx.core.Verticle;

public interface BeforeStart {

    void doBeforeStart(Verticle verticle);

}
