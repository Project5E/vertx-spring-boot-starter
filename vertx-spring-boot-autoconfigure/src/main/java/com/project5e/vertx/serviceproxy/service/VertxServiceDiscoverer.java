package com.project5e.vertx.serviceproxy.service;

import io.vertx.core.Verticle;

import java.util.Collection;

public interface VertxServiceDiscoverer {

    Collection<VertxServiceDefinition> findVertxServices();

    Collection<VertxServiceDefinition> findVertxServices(Verticle verticle);

}
