package com.project5e.vertx.web.exception;

public class ReturnTypeWrongException extends RuntimeException {

    @Override
    public String getMessage() {
        return "return type wrong!";
    }
}
