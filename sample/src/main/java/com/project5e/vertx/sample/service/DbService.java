package com.project5e.vertx.sample.service;

import com.project5e.vertx.sample.verticle.CalculateVerticle;
import com.project5e.vertx.serviceproxy.annotation.VertxService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
@VertxService(address = "calculate.bus", register = CalculateVerticle.class)
public class DbService implements IDbService {

    @Autowired
    PgPool pgPool;

    @PostConstruct
    public void init() {
        log.info(this.getClass().getTypeName() + " init!");
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            queryContent(4141, event -> log.info(event.result().encodePrettily()));
        }).start();
    }

    @Override
    public void queryContent(Integer id, Handler<AsyncResult<JsonObject>> resultHandler) {
        pgPool.preparedQuery("SELECT * FROM content WHERE id = $1")
                .execute(Tuple.of(id))
                .onSuccess(row -> {
                    JsonObject json = null;
                    if (row.size() == 1) {
                        json = row.iterator().next().toJson();
                    }
                    resultHandler.handle(Future.succeededFuture(json));
                })
                .onFailure(event -> resultHandler.handle(Future.failedFuture(event.getCause())));
    }

}
