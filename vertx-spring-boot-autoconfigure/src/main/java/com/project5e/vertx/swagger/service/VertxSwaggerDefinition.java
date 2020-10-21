package com.project5e.vertx.swagger.service;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.Data;

import java.util.List;

@Data
public class VertxSwaggerDefinition {

    Info info;
    List<SecurityRequirement> securityRequirements;
    ExternalDocumentation externalDocumentation;
    List<Tag> tags;
    List<Server> servers;

    public void fillToOpenApi(OpenAPI openAPI) {
        openAPI
            .info(info)
            .tags(tags)
            .servers(servers)
            .security(securityRequirements)
            .externalDocs(externalDocumentation);
    }
}
