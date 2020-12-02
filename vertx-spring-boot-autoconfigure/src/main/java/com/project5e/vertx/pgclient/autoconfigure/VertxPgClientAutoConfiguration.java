package com.project5e.vertx.pgclient.autoconfigure;

import com.project5e.vertx.core.autoconfigure.VertxAutoConfiguration;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Vertx.class, PgPool.class})
@EnableConfigurationProperties(VertxPgClientProperties.class)
@AutoConfigureAfter(VertxAutoConfiguration.class)
public class VertxPgClientAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public PgConnectOptions pgConnectOptions(final VertxPgClientProperties properties) {
        PgConnectOptions options = new PgConnectOptions();
        options.setHost(properties.getHost());
        options.setPort(properties.getPort());
        options.setDatabase(properties.getDatabase());
        assert properties.getUser() != null;
        options.setUser(properties.getUser());
        assert properties.getPassword() != null;
        options.setPassword(properties.getPassword());
        options.setCachePreparedStatements(properties.isCachePreparedStatements());
        options.setPreparedStatementCacheMaxSize(properties.getPreparedStatementCacheMaxSize());
        options.setPreparedStatementCacheSqlLimit(properties.getPreparedStatementCacheSqlLimit());
        if (properties.getProperties() != null) {
            properties.getProperties().forEach(options::addProperty);
        }
        return options;
    }

    @ConditionalOnMissingBean
    @Bean
    public PoolOptions pgPoolOptions(final VertxPgClientProperties properties) {
        PoolOptions options = new PoolOptions();
        if (properties.getPool() != null) {
            options.setMaxSize(properties.getPool().getMaxSize());
            options.setMaxWaitQueueSize(properties.getPool().getMaxWaitQueueSize());
        }
        return options;
    }

    @ConditionalOnMissingBean
    @ConditionalOnBean({Vertx.class, PgConnectOptions.class, PoolOptions.class})
    @Bean
    public PgPool pgPool(final Vertx vertx, final PgConnectOptions connectOptions, final PoolOptions poolOptions) {
        return PgPool.pool(vertx, connectOptions, poolOptions);
    }

}
