package com.project5e.vertx.swagger.servicefactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project5e.vertx.swagger.service.VertxSwaggerDefinition;
import com.project5e.vertx.swagger.service.VertxSwaggerReader;
import com.project5e.vertx.web.service.ProcessResult;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxSwaggerGenerator {

    private final Router router;
    private final ProcessResult processResult;
    private final VertxSwaggerDefinition vertxSwaggerDefinition;

    public VertxSwaggerGenerator(
        ProcessResult processResult, Router router, VertxSwaggerDefinition vertxSwaggerDefinition
    ) {
        this.router = router;
        this.processResult = processResult;
        this.vertxSwaggerDefinition = vertxSwaggerDefinition;
    }

    public void generateAndMount() {
        log.info("generating swagger specification file...");
        VertxSwaggerReader reader = new VertxSwaggerReader();
        OpenAPI openAPI = reader.read(processResult);
        vertxSwaggerDefinition.fillToOpenApi(openAPI);
        router.route("/*").handler(StaticHandler.create());
        router.get("/swagger/openapi.yaml").handler(ctx -> {
            try {
                ctx.end(Yaml.pretty().writeValueAsString(openAPI));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        log.info("generate success. mounted to /swagger/openapi.yaml");
    }

}
