package com.project5e.vertx.serviceproxy.service;

import io.vertx.core.Verticle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VertxServiceDefinition {

    private String address;

    private Verticle verticle;

    private Object service;

    private Class<Object> serviceInterface;

}
