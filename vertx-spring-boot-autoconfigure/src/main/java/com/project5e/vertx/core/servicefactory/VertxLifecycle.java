package com.project5e.vertx.core.servicefactory;

import com.project5e.vertx.core.autoconfigure.VertxProperties;
import com.project5e.vertx.core.service.VerticleDefinition;
import com.project5e.vertx.core.service.VertxServiceDefinition;
import com.project5e.vertx.core.service.VertxServiceDiscoverer;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.context.SmartLifecycle;

import java.util.Collection;

public class VertxLifecycle implements SmartLifecycle {

    private volatile Vertx vertx;
    private final VertxProperties properties;
    private final VertxServiceDiscoverer discoverer;

    public VertxLifecycle(VertxProperties properties, VertxServiceDiscoverer discoverer) {
        this.properties = properties;
        this.discoverer = discoverer;
    }

    @Override
    public void start() {
        Collection<VerticleDefinition> verticleDefinitions = discoverer.findVerticles();
        createVertx(properties);
        createAndDeployVerticle(verticleDefinitions);
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return vertx != null;
    }

    private void createVertx(VertxProperties properties) {
        VertxOptions options = new VertxOptions();
        options.setWorkerPoolSize(properties.getWorkerPoolSize());
        vertx = Vertx.vertx(options);
    }

    private void createAndDeployVerticle(Collection<VerticleDefinition> verticleDefinitions) {
        for (VerticleDefinition definition : verticleDefinitions) {
            vertx.deployVerticle(definition.getVerticle()).result();
        }
    }

}
