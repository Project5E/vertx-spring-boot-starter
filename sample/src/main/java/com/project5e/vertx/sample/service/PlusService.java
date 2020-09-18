package com.project5e.vertx.sample.service;

import com.project5e.vertx.core.autoconfigure.VertxProperties;
import com.project5e.vertx.sample.verticle.NumberVerticle;
import com.project5e.vertx.serviceproxy.annotation.VertxService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Slf4j
@VertxService(address = "plus.bus", register = NumberVerticle.class)
public class PlusService implements IPlusService {

    @Autowired
    Vertx vertx;
    @Autowired
    VertxProperties vertxProperties;

    @PostConstruct
    public void init() {
        log.info(this.getClass().getTypeName() + " init!, vertxProperties = " + vertxProperties.toString());
    }

    @Override
    public void plus(Integer a, Integer b, Handler<AsyncResult<Integer>> resultHandler) {
        Integer res = a + b;
        log.info("plus result = {}", res);
        resultHandler.handle(Future.succeededFuture(res));
    }

    public String getName() {
        return this.getClass().getTypeName();
    }

}
