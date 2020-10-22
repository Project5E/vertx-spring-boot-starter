package com.project5e.vertx.web.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = VertxWebProperties.PREFIX)
public class VertxWebProperties {

    static final String PREFIX = "vertx.server";

    private int port = 8080;

}
