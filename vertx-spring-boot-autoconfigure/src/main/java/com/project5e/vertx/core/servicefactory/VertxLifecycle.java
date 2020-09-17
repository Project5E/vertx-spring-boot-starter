package com.project5e.vertx.core.servicefactory;

import com.project5e.vertx.core.service.VerticleDefinition;
import com.project5e.vertx.core.service.VerticleDiscoverer;
import io.vertx.core.Vertx;
import org.springframework.context.SmartLifecycle;

import java.util.Collection;

public class VertxLifecycle implements SmartLifecycle {

    private boolean running;
    private final Vertx vertx;
    private final VerticleDiscoverer discoverer;

    public VertxLifecycle(Vertx vertx, VerticleDiscoverer discoverer) {
        this.vertx = vertx;
        this.discoverer = discoverer;
    }

    @Override
    public void start() {
        Collection<VerticleDefinition> verticleDefinitions = discoverer.findVerticles();
        createAndDeployVerticle(verticleDefinitions);
        running = true;
    }

    @Override
    public void stop() {
        vertx.close().onComplete(event -> running = false);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void createAndDeployVerticle(Collection<VerticleDefinition> verticleDefinitions) {
        for (VerticleDefinition definition : verticleDefinitions) {
            vertx.deployVerticle(definition.getVerticleProxy()).result();
        }
    }

}
