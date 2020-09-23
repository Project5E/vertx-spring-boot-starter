package com.project5e.vertx.web.exception;

public class EmptyMethodsException extends RuntimeException {

    @Override
    public String getMessage() {
        return "RequestMapping method is null!";
    }
}
