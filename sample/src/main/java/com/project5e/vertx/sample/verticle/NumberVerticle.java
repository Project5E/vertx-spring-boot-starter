package com.project5e.vertx.sample.verticle;

import com.project5e.vertx.core.annotation.Verticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Verticle
public class NumberVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        log.info("start");
        startPromise.complete();
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        log.info("stop");
        stopPromise.complete();
    }

}
