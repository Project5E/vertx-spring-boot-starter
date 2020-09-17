package com.project5e.vertx.sample.verticle;

import com.project5e.vertx.core.annotation.Verticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Verticle
public class CalculateVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        log.info("start Verticle [{}]", this.getClass().getSimpleName());
        startPromise.complete();
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        log.info("stop Verticle [{}]", this.getClass().getSimpleName());
        stopPromise.complete();
    }

}
