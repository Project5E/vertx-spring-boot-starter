package com.project5e.vertx.sample.service;

import com.project5e.vertx.sample.verticle.NumberVerticle;
import com.project5e.vertx.serviceproxy.annotation.VertxService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

@Slf4j
@VertxService(address = "subtract.bus", register = NumberVerticle.class)
public class SubtractService implements ISubtractService {

    @PostConstruct
    public void init() {
        log.info(this.getClass().getTypeName() + " init!");
    }

    @Override
    public void subtract(Integer a, Integer b, Handler<AsyncResult<Integer>> resultHandler) {
        Integer res = a - b;
        log.info("subtract result = {}", res);
        resultHandler.handle(Future.succeededFuture(res));
    }
}
