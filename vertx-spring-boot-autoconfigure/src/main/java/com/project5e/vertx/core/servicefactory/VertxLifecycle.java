package com.project5e.vertx.core.servicefactory;

import com.project5e.vertx.core.service.VerticleDefinition;
import com.project5e.vertx.core.service.VerticleDiscoverer;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

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
        List<Verticle> verticleProxyList = verticleDefinitions.stream()
                .map(VerticleDefinition::getVerticleProxy)
                .collect(Collectors.toList());
        // 处理排序
        AnnotationAwareOrderComparator.sort(verticleProxyList);
        // TODO 异常处理方式感觉不太好
        Thread t = Thread.currentThread();
        CompletableFuture<Throwable> ef = new CompletableFuture<>();
        Semaphore semaphore = new Semaphore(1);
        for (Verticle proxy : verticleProxyList) {
            try {
                semaphore.acquire();
            } catch (Exception e) {
                throw ef.get();
            }
            vertx.deployVerticle(proxy)
                    .onSuccess(event -> {
                        semaphore.release();
                    })
                    .onFailure(e -> {
                        ef.complete(e);
                        t.interrupt();
                    });
        }
    }

}
