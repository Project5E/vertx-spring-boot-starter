package com.project5e.vertx.sample.verticle;

import com.project5e.vertx.core.annotation.ExecuteBlocking;
import com.project5e.vertx.core.annotation.Verticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.util.concurrent.TimeUnit;

@Slf4j
@Order(1)
@Verticle
public class NumberVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        log.info("start Verticle [{}]", this.getClass().getSimpleName());
//        runBlocking(3).onSuccess(res -> log.info("runBlocking onSuccess"));
        startPromise.complete();
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        log.info("stop Verticle [{}]", this.getClass().getSimpleName());
        stopPromise.complete();
    }

    @ExecuteBlocking
    public Future<Integer> runBlocking(int timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Future.succeededFuture(timeout);
    }

}
