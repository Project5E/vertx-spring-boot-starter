package com.project5e.vertx.serviceproxy.servicefactory;

import com.project5e.vertx.core.aop.BeforeStart;
import com.project5e.vertx.serviceproxy.service.VertxServiceDefinition;
import com.project5e.vertx.serviceproxy.service.VertxServiceDiscoverer;
import io.vertx.core.Vertx;
import org.springframework.context.SmartLifecycle;

import java.util.Collection;

public class VertxServiceLifecycle implements SmartLifecycle{

    private boolean running;
    private final Vertx vertx;
    private final VertxServiceDiscoverer discoverer;

    public VertxServiceLifecycle(Vertx vertx, VertxServiceDiscoverer discoverer) {
        this.vertx = vertx;
        this.discoverer = discoverer;
    }

    @Override
    public void start() {
        Collection<VertxServiceDefinition> serviceDefinitions = discoverer.findVertxServices();
        registerVertxService(serviceDefinitions);
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

    private void registerVertxService(Collection<VertxServiceDefinition> serviceDefinitions) {
        for (VertxServiceDefinition definition : serviceDefinitions) {
            definition.getVerticle();
            vertx.deployVerticle(definition.getVerticle()).result();
        }
    }

}
