package com.project5e.vertx.web.exception;

import com.project5e.vertx.web.annotation.HttpMethod;

public class MappingDuplicateException extends RuntimeException {

    private final String mapping;

    public MappingDuplicateException(HttpMethod httpMethod, String path) {
        mapping = httpMethod.name() + " " +  path;
    }

    @Override
    public String getMessage() {
        return "Mapping duplicate! " + mapping;
    }
}
