package com.project5e.vertx.data.redis.autoconfigure;

/**
 * @author: tk
 * @since: 2020/11/1
 */
public interface ReconnectHandler {

    /**
     * reconnect when previous connection was failed
     * @see io.vertx.redis.client.impl.RedisStandaloneConnection#fail(Throwable)
     * @param error the error when previous connection failed
     */
    void handleReconnect(Throwable error);
}
