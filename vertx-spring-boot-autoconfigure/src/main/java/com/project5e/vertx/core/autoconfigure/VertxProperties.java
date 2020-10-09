package com.project5e.vertx.core.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
@ConfigurationProperties(prefix = VertxProperties.PREFIX)
public class VertxProperties {

    public static final String PREFIX = "vertx";

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration blockedThreadCheckInterval;

    private Integer eventLoopPoolSize;

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration maxEventLoopExecuteTime;

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration maxWorkerExecuteTime;

    private Metrics metrics;

    private Boolean preferNativeTransport;

    private Integer quorumSize;

    private Integer workerPoolSize;

    @Data
    public static class Metrics {

        private Boolean enable;

    }

}
