package com.project5e.vertx.web.exception;

public class EmptyPathsException extends RuntimeException {

    @Override
    public String getMessage() {
        return "RequestMapping path is null!";
    }
}
