package com.project5e.vertx.sample.configuration;

import com.project5e.vertx.web.annotation.ExceptionHandler;
import com.project5e.vertx.web.annotation.RouterAdvice;
import com.project5e.vertx.web.component.ResponseEntity;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolationException;

@Slf4j
@RouterAdvice
public class MyExceptionHandler {

//    @ExceptionHandler(IllegalArgumentException.class)
//    public Future<String> exceptionHandler(RoutingContext routingContext, Exception exception) {
//        log.error(exception.getMessage(), exception);
//        return Future.succeededFuture(exception.getMessage());
//    }

    // TODO 未实现，可能不需要
//    @ExceptionHandler(IllegalArgumentException.class)
//    public void exceptionHandler1(RoutingContext routingContext, Exception exception) {
//        log.error(exception.getMessage(), exception);
//        routingContext.response().setStatusCode(500);
//    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Future<ResponseEntity<String>> exceptionHandler2(ConstraintViolationException e) {
        log.error(e.getMessage(), e);
        return Future.succeededFuture(ResponseEntity.completeWithPlainText(400, e.getMessage()));
    }


    @ExceptionHandler(NullPointerException.class)
    public Future<ResponseEntity<String>> exceptionHandler3(NullPointerException e) {
        log.error(e.getMessage(), e);
        return Future.succeededFuture(ResponseEntity.completeWithPlainText(400, e.getMessage()));
    }

}
