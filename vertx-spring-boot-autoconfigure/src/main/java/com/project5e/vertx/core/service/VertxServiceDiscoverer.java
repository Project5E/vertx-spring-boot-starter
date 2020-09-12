package com.project5e.vertx.core.service;

import java.util.Collection;

public interface VertxServiceDiscoverer {

    Collection<VerticleDefinition> findVerticles();

    Collection<VertxServiceDefinition> findVertxServices();

}
