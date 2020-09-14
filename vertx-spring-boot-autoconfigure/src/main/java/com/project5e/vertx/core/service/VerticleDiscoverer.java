package com.project5e.vertx.core.service;

import java.util.Collection;

public interface VerticleDiscoverer {

    Collection<VerticleDefinition> findVerticles();

//    Collection<VertxServiceDefinition> findVertxServices();

}
