package com.project5e.vertx.serviceproxy.service;

import java.util.Collection;

public interface VertxServiceDiscoverer {

    Collection<VertxServiceDefinition> findVertxServices();

}
