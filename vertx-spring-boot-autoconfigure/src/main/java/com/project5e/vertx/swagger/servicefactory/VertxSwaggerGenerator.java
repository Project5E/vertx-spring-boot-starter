package com.project5e.vertx.swagger.servicefactory;

import cn.hutool.crypto.digest.MD5;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.project5e.vertx.swagger.service.VertxSwaggerDefinition;
import com.project5e.vertx.swagger.service.VertxSwaggerReader;
import com.project5e.vertx.web.service.ProcessResult;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class VertxSwaggerGenerator {

    private final Router router;
    private final ProcessResult processResult;
    private final List<VertxSwaggerDefinition> vertxSwaggerDefinitions;

    public VertxSwaggerGenerator(
        ProcessResult processResult, Router router, List<VertxSwaggerDefinition> vertxSwaggerDefinitions
    ) {
        this.router = router;
        this.processResult = processResult;
        this.vertxSwaggerDefinitions = vertxSwaggerDefinitions;
    }

    public void generateAndMount() {
        log.info("generating swagger specification file...");

        router.route("/*").handler(StaticHandler.create());

        Map<String, String> openApiMap = new HashMap<>();
        for (String page : VertxSwaggerReader.parsePageKeys(processResult)) {
            OpenAPI openAPI = new VertxSwaggerReader().read(processResult, page);
            // page可以是中文，但中文不可出现在url，这里做个摘要
            String openApiName = String.format("openapi-%s.yaml", MD5.create().digestHex(page));
            String openApiPath = String.format("/swagger/%s", openApiName);
            openApiMap.put(page, openApiName);

            Optional<VertxSwaggerDefinition> definition = vertxSwaggerDefinitions.stream()
                .filter(item -> item.getBelongsToPages().contains(page))
                .findFirst();

            if (definition.isPresent()) {
                definition.get().fillToOpenApi(openAPI);
            } else {
                log.error("can not find VertxSwaggerDefinition for page: " + page);
            }

            router.get(openApiPath).handler(ctx -> {
                try {
                    ctx.end(Yaml.pretty().writeValueAsString(openAPI));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
            log.info("generate success. mounted to " + openApiPath);
        }

        router.route("/swagger/").handler(ctx -> ctx.redirect("/swagger/index1.html"));
        router.route("/swagger/index1.html").handler(ctx -> {
            List<String> segments = openApiMap.entrySet().stream()
                .map(entry -> String.format("{url: '%s', name: '%s'}", entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
            ctx.end(String.format(indexTemplate, StringUtils.join(segments, " , ")));
        });

    }

    private final String indexTemplate = "<!-- HTML for static distribution bundle build -->\n" +
        "<!DOCTYPE html>\n" +
        "<html lang=\"en\">\n" +
        "  <head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <title>Swagger UI</title>\n" +
        "    <link rel=\"stylesheet\" type=\"text/css\" href=\"./swagger-ui.css\" >\n" +
        "    <link rel=\"icon\" type=\"image/png\" href=\"./favicon-32x32.png\" sizes=\"32x32\" />\n" +
        "    <link rel=\"icon\" type=\"image/png\" href=\"./favicon-16x16.png\" sizes=\"16x16\" />\n" +
        "    <style>\n" +
        "      html\n" +
        "      {\n" +
        "        box-sizing: border-box;\n" +
        "        overflow: -moz-scrollbars-vertical;\n" +
        "        overflow-y: scroll;\n" +
        "      }\n" +
        "\n" +
        "      *,\n" +
        "      *:before,\n" +
        "      *:after\n" +
        "      {\n" +
        "        box-sizing: inherit;\n" +
        "      }\n" +
        "\n" +
        "      body\n" +
        "      {\n" +
        "        margin:0;\n" +
        "        background: #fafafa;\n" +
        "      }\n" +
        "    </style>\n" +
        "  </head>\n" +
        "\n" +
        "  <body>\n" +
        "    <div id=\"swagger-ui\"></div>\n" +
        "\n" +
        "    <script src=\"./swagger-ui-bundle.js\" charset=\"UTF-8\"> </script>\n" +
        "    <script src=\"./swagger-ui-standalone-preset.js\" charset=\"UTF-8\"> </script>\n" +
        "    <script>\n" +
        "    window.onload = function() {\n" +
        "      // Begin Swagger UI call region\n" +
        "      const ui = SwaggerUIBundle({\n" +
        "        urls: [%s],\n" +
        "        dom_id: '#swagger-ui',\n" +
        "        deepLinking: true,\n" +
        "        presets: [\n" +
        "          SwaggerUIBundle.presets.apis,\n" +
        "          SwaggerUIStandalonePreset\n" +
        "        ],\n" +
        "        plugins: [\n" +
        "          SwaggerUIBundle.plugins.DownloadUrl\n" +
        "        ],\n" +
        "        layout: \"StandaloneLayout\"\n" +
        "      })\n" +
        "      // End Swagger UI call region\n" +
        "\n" +
        "      window.ui = ui\n" +
        "    }\n" +
        "  </script>\n" +
        "  </body>\n" +
        "</html>\n";
}
