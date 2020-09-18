package com.project5e.vertx.sample.service;

import com.project5e.vertx.sample.verticle.CalculateVerticle;
import com.project5e.vertx.sample.verticle.NumberVerticle;
import com.project5e.vertx.serviceproxy.annotation.VertxService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
@VertxService(address = "calculate.bus", register = CalculateVerticle.class)
public class CalculateService implements ICalculateService {

    @Autowired
    PlusService plusService;
    @Autowired
    IPlusService iPlusService;
    @Autowired
    ISubtractService iSubtractService;

    @PostConstruct
    public void init() {
        log.info(this.getClass().getTypeName() + " init!");
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            plus(1, 2, event -> log.info("plus res = " + event.result()));
            subtract(1, 2, event -> log.info("subtract res = " + event.result()));
        }).start();
    }

    @Override
    public void plus(Integer a, Integer b, Handler<AsyncResult<Integer>> resultHandler) {
        log.info("plus" + plusService.getName());
        iPlusService.plus(a, b, resultHandler);
    }

    @Override
    public void subtract(Integer a, Integer b, Handler<AsyncResult<Integer>> resultHandler) {
        iSubtractService.subtract(a, b, resultHandler);
    }
}
