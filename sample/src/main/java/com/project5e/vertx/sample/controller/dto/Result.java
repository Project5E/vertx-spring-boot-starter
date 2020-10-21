package com.project5e.vertx.sample.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class Result<T> {

    private String requestId;
    private int page;
    private int size;

    private List<T> data;
}
