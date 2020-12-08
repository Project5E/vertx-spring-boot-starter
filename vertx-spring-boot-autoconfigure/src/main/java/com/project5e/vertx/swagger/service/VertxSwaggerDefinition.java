package com.project5e.vertx.swagger.service;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
public class VertxSwaggerDefinition {

    // 一个定义可以给多个page使用
    List<String> belongsToPages = Arrays.asList(VertxSwaggerReader.DEFAULT_PAGE_KEY);
    Info info;
    List<SecurityRequirement> securityRequirements;
    ExternalDocumentation externalDocumentation;
    List<Tag> tags;
    List<Server> servers;
    Map<String, SecurityScheme> securitySchemes;

    public void fillToOpenApi(OpenAPI openAPI) {
        openAPI
            .info(info)
            .tags(tags)
            .servers(servers)
            .security(securityRequirements)
            .externalDocs(externalDocumentation);
        Components components = openAPI.getComponents();
        if (components == null) {
            components = new Components();
        }
        openAPI.components(components.securitySchemes(securitySchemes));
    }
}
