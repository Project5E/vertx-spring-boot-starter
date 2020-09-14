package com.project5e.vertx.core.servicefactory;

import com.project5e.vertx.core.service.VerticleDefinition;
import com.project5e.vertx.core.service.VerticleDiscoverer;
import io.vertx.core.Vertx;
import org.springframework.context.SmartLifecycle;

import java.util.Collection;

public class VertxLifecycle implements SmartLifecycle {

    private volatile Vertx vertx;
    private final VerticleDiscoverer discoverer;

    public VertxLifecycle(Vertx vertx, VerticleDiscoverer discoverer) {
        this.vertx = vertx;
        this.discoverer = discoverer;
    }

    @Override
    public void start() {
        Collection<VerticleDefinition> verticleDefinitions = discoverer.findVerticles();
        createAndDeployVerticle(verticleDefinitions);
    }

    @Override
    public void stop() {
        vertx.close().onComplete(event -> vertx = null);
    }

    @Override
    public boolean isRunning() {
        return vertx != null;
    }

    private void createAndDeployVerticle(Collection<VerticleDefinition> verticleDefinitions) {
        for (VerticleDefinition definition : verticleDefinitions) {
            vertx.deployVerticle(definition.getVerticle()).result();
        }
    }

}
