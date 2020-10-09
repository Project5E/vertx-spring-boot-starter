package com.project5e.vertx.core.autoconfigure;

import com.project5e.vertx.core.aop.VerticleAnnotationAdvisor;
import com.project5e.vertx.core.aop.VerticleAnnotationBeanPostProcessor;
import com.project5e.vertx.core.aop.VerticleAnnotationInterceptor;
import com.project5e.vertx.core.service.AnnotationVerticleDiscoverer;
import com.project5e.vertx.core.service.VerticleDiscoverer;
import com.project5e.vertx.core.servicefactory.VertxLifecycle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.tracing.TracingOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Vertx.class)
@EnableConfigurationProperties(VertxProperties.class)
public class VertxAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public Vertx vertx(final VertxProperties properties) {
        VertxOptions options = new VertxOptions();
        if (properties.getEventLoopPoolSize() != null) {
            options.setEventLoopPoolSize(properties.getEventLoopPoolSize());
        }
        if (properties.getBlockedThreadCheckInterval() != null) {
            options.setBlockedThreadCheckInterval(properties.getBlockedThreadCheckInterval().getSeconds());
            options.setBlockedThreadCheckIntervalUnit(TimeUnit.SECONDS);
        }
        if (properties.getMaxEventLoopExecuteTime() != null) {
            options.setMaxEventLoopExecuteTime(properties.getMaxEventLoopExecuteTime().getSeconds());
            options.setMaxEventLoopExecuteTimeUnit(TimeUnit.SECONDS);
        }
        if (properties.getMaxWorkerExecuteTime() != null) {
            options.setMaxWorkerExecuteTime(properties.getMaxWorkerExecuteTime().getSeconds());
            options.setMaxWorkerExecuteTimeUnit(TimeUnit.SECONDS);
        }
        VertxProperties.Metrics metrics = properties.getMetrics();
        if (metrics != null) {
            MetricsOptions metricsOptions = new MetricsOptions();
            if (metrics.getEnable() != null) {
                metricsOptions.setEnabled(metrics.getEnable());
            }
            options.setMetricsOptions(metricsOptions);
        }
        if (properties.getPreferNativeTransport() != null) {
            options.setPreferNativeTransport(properties.getPreferNativeTransport());
        }
        if (properties.getQuorumSize() != null) {
            options.setQuorumSize(properties.getQuorumSize());
        }
        if (properties.getWorkerPoolSize() != null) {
            options.setWorkerPoolSize(properties.getWorkerPoolSize());
        }
        return Vertx.vertx(options);
    }

    @ConditionalOnMissingBean
    @Bean
    public VerticleDiscoverer verticleDiscoverer() {
        return new AnnotationVerticleDiscoverer();
    }

    @ConditionalOnMissingBean
    @Bean
    public VerticleAnnotationInterceptor verticleAnnotationInterceptor() {
        return new VerticleAnnotationInterceptor();
    }

    @ConditionalOnMissingBean
    @ConditionalOnBean(VerticleAnnotationInterceptor.class)
    @Bean
    public VerticleAnnotationBeanPostProcessor verticleAnnotationBeanPostProcessor(final VerticleAnnotationInterceptor interceptor) {
        return new VerticleAnnotationBeanPostProcessor(new VerticleAnnotationAdvisor(interceptor));
    }

    @ConditionalOnMissingBean
    @ConditionalOnBean({Vertx.class, VerticleDiscoverer.class})
    @Bean
    public VertxLifecycle vertxLifecycle(final Vertx vertx, final VerticleDiscoverer discoverer) {
        return new VertxLifecycle(vertx, discoverer);
    }

}
