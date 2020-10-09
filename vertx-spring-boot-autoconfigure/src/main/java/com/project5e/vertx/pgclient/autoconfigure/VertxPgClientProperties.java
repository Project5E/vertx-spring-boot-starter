package com.project5e.vertx.pgclient.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = VertxPgClientProperties.PREFIX)
public class VertxPgClientProperties {

    public static final String PREFIX = "vertx.pg-client";

    private String host = "localhost";

    private int port = 5432;

    private String user;

    private String password;

    private String database;

    private boolean cachePreparedStatements = false;

    private int preparedStatementCacheMaxSize = 256;

    private int preparedStatementCacheSqlLimit = 2048;

    private Map<String, String> properties;

    private Pool pool;

    @Data
    public static class Pool {

        private int maxSize = 5;

        private int maxWaitQueueSize = -1;
    }

}
