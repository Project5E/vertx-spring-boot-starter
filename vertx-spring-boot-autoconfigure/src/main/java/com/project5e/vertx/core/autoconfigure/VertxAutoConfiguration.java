package com.project5e.vertx.core.autoconfigure;

import com.project5e.vertx.core.service.AnnotationVertxServiceDiscoverer;
import com.project5e.vertx.core.service.VertxServiceDiscoverer;
import com.project5e.vertx.core.servicefactory.VertxLifecycle;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Vertx.class)
@EnableConfigurationProperties(VertxProperties.class)
public class VertxAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public VertxServiceDiscoverer defaultVertxServiceDiscoverer() {
        return new AnnotationVertxServiceDiscoverer();
    }

    @ConditionalOnMissingBean
    @ConditionalOnBean(VertxServiceDiscoverer.class)
    @Bean
    public VertxLifecycle verticleLifecycle(final VertxProperties properties, VertxServiceDiscoverer discoverer) {
        return new VertxLifecycle(properties, discoverer);
    }

}
