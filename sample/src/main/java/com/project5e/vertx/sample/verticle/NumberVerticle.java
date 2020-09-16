package com.project5e.vertx.sample.verticle;

import com.project5e.vertx.core.annotation.ExecuteBlocking;
import com.project5e.vertx.core.annotation.Verticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@Verticle
public class NumberVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        log.info("start");
//        runBlocking(3).onSuccess(res -> log.info("runBlocking onSuccess"));
        startPromise.complete();
    }

    @Override
    public void start() throws Exception {
        super.start();
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        log.info("stop");
        stopPromise.complete();
    }

    @ExecuteBlocking
    public Future<Integer> runBlocking(int timeout){
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Future.succeededFuture(timeout);
    }

}
