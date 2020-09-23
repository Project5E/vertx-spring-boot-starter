package com.project5e.vertx.web.exception;

public class NewInstanceException extends RuntimeException {

    public NewInstanceException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "new instance failed!";
    }
}
