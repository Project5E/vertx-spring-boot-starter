package com.project5e.vertx.core.servicefactory;

import com.project5e.vertx.autoconfigure.VertxProperties;
import com.project5e.vertx.core.service.VertxServiceDefinition;
import com.project5e.vertx.core.service.VertxServiceDiscoverer;
import io.vertx.core.Vertx;
import org.springframework.context.SmartLifecycle;

import java.util.Collection;

public class VerticleLifecycle implements SmartLifecycle {

    private volatile Vertx vertx;
    private final VertxProperties properties;
    private final VertxServiceDiscoverer discoverer;

    public VerticleLifecycle(VertxProperties properties, VertxServiceDiscoverer discoverer) {
        this.properties = properties;
        this.discoverer = discoverer;
    }

    @Override
    public void start() {
        Collection<VertxServiceDefinition> serviceDefinitions = discoverer.findVertxServices();
        createAndStartVerticle(serviceDefinitions);
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return vertx != null;
    }

    private void createAndStartVerticle(Collection<VertxServiceDefinition> serviceDefinitions) {
        vertx = Vertx.vertx();
        configure(vertx, serviceDefinitions);
    }

    private void configure(Vertx vertx, Collection<VertxServiceDefinition> serviceDefinitions) {
        for (VertxServiceDefinition serviceDefinition : serviceDefinitions) {
//            vertx.deployVerticle(serviceDefinition.)
        }
    }

}
