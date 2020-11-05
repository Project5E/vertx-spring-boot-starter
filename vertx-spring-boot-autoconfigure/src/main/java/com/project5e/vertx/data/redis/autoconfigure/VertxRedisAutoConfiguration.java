package com.project5e.vertx.data.redis.autoconfigure;

import com.project5e.vertx.data.redis.exception.IllegalRedisPropertiesException;
import com.project5e.vertx.data.redis.exception.RedisNodeEmptyException;
import com.project5e.vertx.data.redis.exception.RedisConnectionCreateTimeoutException;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.redis.client.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author: tk
 * @since: 2020/10/31
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Redis.class)
@EnableConfigurationProperties(VertxRedisProperties.class)
public class VertxRedisAutoConfiguration implements ApplicationContextAware {
    private static final long CREATE_CLIENT_TIMEOUT = 10000;
    private static final String CONNECTION_PROTOCOL = "redis://";
    private static final String COLON = ":";
    private static final String DIAGONAL = "/";

    private final VertxRedisProperties properties;
    private ApplicationContext applicationContext;
    private final List<ReconnectHandler> reconnectHandlerPipeline = new ArrayList<>();

    public VertxRedisAutoConfiguration(VertxRedisProperties properties) {
        if (BooleanUtils.isFalse(validate(properties))) {
            throw new IllegalRedisPropertiesException();
        }
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        String[] handlerNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, ReconnectHandler.class, true, false);
        for (String name : handlerNames) {
            if (name.equals(ReconnectHandler.class.getName())) {
                continue;
            }
            ReconnectHandler bean = applicationContext.getBean(name, ReconnectHandler.class);
            reconnectHandlerPipeline.add(bean);
        }
    }

    @Bean
    public RedisOptions redisOptions() {
        RedisOptions options = new RedisOptions();

        if (StringUtils.isNotBlank(properties.getUrl())) {
            options.setConnectionString(properties.getUrl() + DIAGONAL + properties.getDatabase());
        } else {
            options.setConnectionString(CONNECTION_PROTOCOL + properties.getHost()
                    + COLON + properties.getPort() + DIAGONAL + properties.getDatabase());
            options.setPassword(properties.getPassword());
        }
        options.setPassword(properties.getPassword());

        resolveSentinel(options);
        resolveCluster(options);

        options.setRole(getRole());
        options.setType(getClientType());
        options.setUseSlave(getSlaves());

        //connection pool initialization
        if (this.properties.getPool().isEnable()) {
            options.setMaxPoolSize(this.properties.getPool().getMaxPoolSize());
            options.setMaxWaitingHandlers(this.properties.getPool().getMaxWaitingHandlers());
            options.setMaxPoolWaiting(this.properties.getPool().getMaxPoolWaiting());
            options.setPoolCleanerInterval(this.properties.getPool().getPoolCleanerInterval());
            options.setMaxPoolWaiting(this.properties.getPool().getMaxPoolWaiting());
        }

        return options;
    }

    @SneakyThrows
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedisOptions.class)
    @ConditionalOnProperty(prefix = VertxRedisProperties.PREFIX, name = "singleConnection", havingValue = "true")
    public RedisConnection connection(Vertx vertx, RedisOptions options) {
        Promise<RedisConnection> promise = Promise.promise();
        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();

        Redis.createClient(vertx, options)
                .connect(onConnect -> {
                    if (onConnect.succeeded()) {
                        promise.complete(onConnect.result());
                        semaphore.release();

                        resolveReconnect(onConnect.result());
                    }
                });
        //wait 10s for redis connection complete
        semaphore.tryAcquire(CREATE_CLIENT_TIMEOUT, TimeUnit.SECONDS);
        RedisConnection client = promise.future().result();
        if (client == null) {
            log.error("timeout when create redis client");
            throw new RedisConnectionCreateTimeoutException();
        }
        return client;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedisOptions.class)
    public Redis client(Vertx vertx, RedisOptions options) {

        return Redis.createClient(vertx, options)
                .connect(onConnect -> {
                    if (onConnect.succeeded()) {
                        log.info("connection in redis client was established successfully");

                        resolveReconnect(onConnect.result());
                    }
                });
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(Redis.class)
    public RedisAPI redisApi(Redis client) {
        return RedisAPI.api(client);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void resolveReconnect(RedisConnection connection) {
        if (CollectionUtils.isNotEmpty(reconnectHandlerPipeline)) {
            connection.exceptionHandler(event -> {
                reconnectHandlerPipeline.forEach(reconnectHandler -> reconnectHandler.handleReconnect(event));
            });
        }
    }

    private RedisClientType getClientType() {
        if (this.properties.getSentinel() != null) {
            return RedisClientType.SENTINEL;
        }
        if (this.properties.getCluster() != null) {
            return RedisClientType.CLUSTER;
        }

        return RedisClientType.STANDALONE;
    }

    private RedisRole getRole() {
        if (this.properties.getSentinel() != null && this.properties.getSentinel().isReadonly()) {
            return RedisRole.SLAVE;
        }

        return RedisRole.MASTER;
    }

    private RedisSlaves getSlaves() {
        VertxRedisProperties.Cluster cluster = this.properties.getCluster();
        if (cluster != null) {
            if (cluster.isAlways()) {
                return RedisSlaves.ALWAYS;
            }
            if (cluster.isShare()) {
                return RedisSlaves.SHARE;
            }
        }

        return RedisSlaves.NEVER;
    }

    private void resolveSentinel(RedisOptions options) {
        VertxRedisProperties.Sentinel sentinel = this.properties.getSentinel();
        if (sentinel != null) {
            if (ArrayUtils.isEmpty(sentinel.getNodes())) {
                throw new RedisNodeEmptyException();
            }

            List<String> endpoints = new ArrayList<>(sentinel.getNodes().length);
            for (String node : sentinel.getNodes()) {
                endpoints.add(CONNECTION_PROTOCOL + node);
            }
            options.setEndpoints(endpoints);

            options.setMasterName(sentinel.getMasterName());
        }
    }

    private void resolveCluster(RedisOptions options) {
        VertxRedisProperties.Cluster cluster = this.properties.getCluster();
        if (cluster != null) {
            if (ArrayUtils.isEmpty(cluster.getNodes())) {
                throw new RedisNodeEmptyException();
            }
            for (String node : cluster.getNodes()) {
                options.addConnectionString(node);
            }
        }
    }

    /**
     * check properties
     *
     * @return false:throw {@link com.project5e.vertx.data.redis.exception.IllegalRedisPropertiesException}
     */
    private boolean validate(VertxRedisProperties properties) {

        return Boolean.TRUE;
    }
}
