package com.project5e.vertx.web.service;

import com.project5e.vertx.web.component.ResponseEntity;
import lombok.Data;

@Data
public class OperationResult {

    private Boolean succeed;
    private Throwable cause;

    private ResponseEntity<?> responseEntity;

}
