package com.project5e.vertx.web.service;

import cn.hutool.http.ContentType;
import lombok.Data;

@Data
public class OperationResult {

    private Boolean succeed;
    private ContentType contentType;
    private Throwable cause;
    private String chunk;

}
