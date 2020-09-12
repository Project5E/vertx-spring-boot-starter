package com.project5e.vertx.core.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = VertxProperties.PREFIX)
public class VertxProperties {

    public static final String PREFIX = "vertx";

    private int workerPoolSize;

}
