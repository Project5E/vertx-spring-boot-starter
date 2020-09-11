package com.project5e.vertx.core.service;

import java.util.Collection;

public interface VertxServiceDiscoverer {

    Collection<VertxServiceDefinition> findVertxServices();

}
