package com.project5e.vertx.core.servicefactory;

import com.project5e.vertx.core.service.VerticleDefinition;
import com.project5e.vertx.core.service.VerticleDiscoverer;
import io.vertx.core.Vertx;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

@Slf4j
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
        List<VerticleDefinition> verticleDefinitions = discoverer.findVerticles();
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

    @SneakyThrows
    private void createAndDeployVerticle(List<VerticleDefinition> verticleDefinitions) {
        verticleDefinitions.sort(Comparator.comparingInt(VerticleDefinition::getOrder));
        Semaphore semaphore = new Semaphore(1);
        for (VerticleDefinition definition : verticleDefinitions) {
            semaphore.acquire();
            vertx.deployVerticle(definition.getVerticleProxy()).onSuccess(event -> {
                semaphore.release();
            }).onFailure(Throwable::printStackTrace);
        }
    }

}
