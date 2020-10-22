package com.project5e.vertx.web.exception;

public class IllegalPathException extends RuntimeException {

    @Override
    public String getMessage() {
        return "illegal path!";
    }
}
