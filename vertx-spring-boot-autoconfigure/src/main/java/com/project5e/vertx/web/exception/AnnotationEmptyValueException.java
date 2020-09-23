package com.project5e.vertx.web.exception;

public class AnnotationEmptyValueException extends RuntimeException {

    @Override
    public String getMessage() {
        return "annotation value is empty!";
    }
}
