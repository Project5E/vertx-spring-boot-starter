package com.project5e.vertx.serviceproxy.servicefactory;

import com.project5e.vertx.serviceproxy.service.VertxServiceDefinition;
import com.project5e.vertx.serviceproxy.service.VertxServiceDiscoverer;
import io.vertx.core.Vertx;
import org.springframework.context.SmartLifecycle;

import java.util.Collection;

public class VertxServiceLifecycle implements SmartLifecycle {

    private volatile Vertx vertx;
    private final VertxServiceDiscoverer discoverer;

    public VertxServiceLifecycle(Vertx vertx, VertxServiceDiscoverer discoverer) {
        this.vertx = vertx;
        this.discoverer = discoverer;
    }

    @Override
    public void start() {
        Collection<VertxServiceDefinition> serviceDefinitions = discoverer.findVertxServices();
        registerVertxService(serviceDefinitions);
    }

    @Override
    public void stop() {
        vertx = null;
    }

    @Override
    public boolean isRunning() {
        return vertx != null;
    }

    private void registerVertxService(Collection<VertxServiceDefinition> serviceDefinitions) {
        for (VertxServiceDefinition definition : serviceDefinitions) {
//            vertx.deployVerticle(definition.getVerticle()).result();
        }
    }

}
