package com.project5e.vertx.core.service;

import lombok.Data;

@Data
public class VerticleDefinition {

    private String beanName;

    private io.vertx.core.Verticle verticle;

}
