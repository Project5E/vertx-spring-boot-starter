package com.project5e.vertx.web.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@Data
@ConfigurationProperties(prefix = VertxWebProperties.PREFIX)
public class VertxWebProperties {

    static final String PREFIX = "vertx.server";

    private int port = 8080;

    private DataSize bodyLimit = null;

}
