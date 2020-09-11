package com.project5e.vertx.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = VertxProperties.PREFIX)
public class VertxProperties {

    public static final String PREFIX = "vertx";

    private int workerPoolSize;

}
