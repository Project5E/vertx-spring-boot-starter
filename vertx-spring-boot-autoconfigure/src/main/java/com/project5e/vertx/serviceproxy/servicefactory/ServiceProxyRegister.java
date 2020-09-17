package com.project5e.vertx.serviceproxy.servicefactory;

import com.project5e.vertx.core.aop.BeforeStart;
import com.project5e.vertx.serviceproxy.service.VertxServiceDiscoverer;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceProxyRegister implements BeforeStart {

    private final Vertx vertx;
    private final VertxServiceDiscoverer discoverer;

    public ServiceProxyRegister(Vertx vertx, VertxServiceDiscoverer discoverer) {
        this.vertx = vertx;
        this.discoverer = discoverer;
    }

    @Override
    public void doBeforeStart() {
        ServiceBinder serviceBinder = new ServiceBinder(vertx);
        discoverer.findVertxServices().forEach(definition -> {
            serviceBinder
                    .setAddress(definition.getAddress())
                    .register(definition.getServiceInterface(), definition.getService());
        });
        log.error("register success");
    }
}
