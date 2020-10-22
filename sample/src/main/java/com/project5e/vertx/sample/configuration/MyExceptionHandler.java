package com.project5e.vertx.sample.configuration;

import com.project5e.vertx.web.annotation.ExceptionHandler;
import com.project5e.vertx.web.annotation.RouterAdvice;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RouterAdvice
public class MyExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Future<String> exceptionHandler(RoutingContext routingContext, Exception exception) {
        log.error(exception.getMessage(), exception);
        return Future.succeededFuture(exception.getMessage());
    }

}
