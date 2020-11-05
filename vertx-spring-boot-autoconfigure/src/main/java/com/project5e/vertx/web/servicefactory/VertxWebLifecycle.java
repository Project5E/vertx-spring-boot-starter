package com.project5e.vertx.web.servicefactory;

import com.project5e.vertx.web.autoconfigure.VertxWebProperties;
import com.project5e.vertx.web.service.HttpRouterGenerator;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

@Slf4j
public class VertxWebLifecycle implements SmartLifecycle {

    private final Vertx vertx;
    private final HttpServer server;
    private final Router router;
    private final VertxWebProperties properties;

    private boolean running;

    public VertxWebLifecycle(Vertx vertx, HttpRouterGenerator generator, VertxWebProperties properties) {
        this.vertx = vertx;
        this.router = generator.generate();
        this.properties = properties;
        this.server = vertx.createHttpServer();
    }

    @Override
    public void start() {
        Router router;
        if (properties.getContextPath() != null) {
            router = Router.router(vertx);
            router.mountSubRouter(properties.getContextPath(), this.router);
        } else {
            router = this.router;
        }
        this.server
                .requestHandler(router)
                .listen(properties.getPort(), "0.0.0.0")
                .onSuccess(event -> {
                    running = true;
                    log.info("Vertx http listen on {}", properties.getPort());
                })
                .onFailure(e -> {
                    // TODO 异常处理
                    log.error("Vertx http server start fail.", e);
                });
    }

    @Override
    public void stop() {
        server
                .close()
                .onComplete(v -> running = false)
                .onFailure(e -> log.error("Exception when stop server", e));
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
