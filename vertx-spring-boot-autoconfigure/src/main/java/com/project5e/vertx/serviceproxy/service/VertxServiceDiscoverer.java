package com.project5e.vertx.serviceproxy.service;

import io.vertx.core.Verticle;

import java.util.List;

public interface VertxServiceDiscoverer {

    List<VertxServiceDefinition> findVertxServices();

    List<VertxServiceDefinition> findVertxServices(Verticle verticle);

}
