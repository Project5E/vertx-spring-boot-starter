package com.project5e.vertx.sample.service;

import com.project5e.vertx.sample.verticle.CalculateVerticle;
import com.project5e.vertx.sample.verticle.NumberVerticle;
import com.project5e.vertx.serviceproxy.annotation.VertxService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Slf4j
@VertxService(address = "calculate.bus", register = CalculateVerticle.class)
public class CalculateService implements ICalculateService {

    @Autowired
    PlusService plusService;
    @Autowired
    SubtractService subtractService;

    @PostConstruct
    public void init() {
        log.info(this.getClass().getTypeName() + " init!");
    }

    @Override
    public void plus(Integer a, Integer b, Handler<AsyncResult<Integer>> resultHandler) {
        plusService.plus(a, b, resultHandler);
    }

    @Override
    public void subtract(Integer a, Integer b, Handler<AsyncResult<Integer>> resultHandler) {
        subtractService.subtract(a, b, resultHandler);
    }
}
