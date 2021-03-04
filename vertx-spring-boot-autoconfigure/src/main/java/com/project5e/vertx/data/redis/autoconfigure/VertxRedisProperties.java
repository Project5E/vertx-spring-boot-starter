package com.project5e.vertx.data.redis.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: tk
 * @since: 2020/10/31
 */
@Data
@ConfigurationProperties(prefix = VertxRedisProperties.PREFIX)
public class VertxRedisProperties {
    public static final String PREFIX = "vertx.redis";

    /**
     * Connection URL. Overrides host, port, and password. User is ignored. Example:
     * redis://user:password@example.com:6379
     */
    private String url;

    /**
     * if choose sentinel or cluster,it can be null
     */
    private String host = "localhost";

    private int port = 6379;

    /**
     * Database index used by the connection factory.
     */
    private int database = 0;

    private String password;

    private Sentinel sentinel;

    private Cluster cluster;

    private Pool pool = Pool.DEFAULT;

    /**
     * aware a redis connection to beanFactory
     */
    private boolean singleConnection = false;

    @Data
    public static class Sentinel {
        /**
         * Comma-separated list of "host:port" pairs.
         */
        private String[] nodes;

        /**
         * Name of the Redis server.
         */
        private String masterName = "myMaster";

        /**
         * Password for authenticating with sentinel(s).
         */
        private String password;

        /**
         * if true means just Use a SLAVE node connection.
         */
        private boolean readonly = false;
    }

    @Data
    public static class Cluster {

        /**
         * Comma-separated list of "host:port" pairs to bootstrap from. This represents an
         * "initial" list of cluster nodes and is required to have at least one entry.
         */
        private String[] nodes;

        /**
         * Never use SLAVES, queries are always run on a MASTER node.
         */
        private boolean never = true;

        /**
         * Queries can be randomly run on both MASTER and SLAVE nodes.
         *
         * @see io.vertx.redis.client.impl.RedisClusterConnection#selectMasterOrSlaveEndpoint
         */
        private boolean share = false;

        /**
         * Queries are always run on SLAVE nodes (never on MASTER node).
         */
        private boolean always = false;
    }


    /**
     * redis connection pool properties
     */
    @Data
    public static class Pool {
        private static final Pool DEFAULT = new Pool();

        /**
         * whether enable redis pool(default true)
         */
        private boolean enable = true;

        /**
         * the maximum number of connections on the pool(default 6)
         */
        private int maxPoolSize = 6;

        /**
         * the maximum waiting handlers to get a connection on a queue (default 24)
         */
        private int maxPoolWaiting = 24;

        /**
         * allow how much connection requests to queue waiting
         * for a connection to be available.(default 2048)
         */
        private int maxWaitingHandlers = 2048;

        /**
         * the interval when connections will be clean default is -1 (disabled)
         */
        private int poolCleanerInterval = -1;

        /**
         * the timeout to keep an open connection on the pool waiting and then close (default 15_000)
         */
        private int poolRecycleTimeout = 15_000;
    }
}
