package com.project5e.vertx.serviceproxy.autoconfigure;

import com.project5e.vertx.core.autoconfigure.VertxAutoConfiguration;
import com.project5e.vertx.core.service.VerticleDiscoverer;
import com.project5e.vertx.serviceproxy.service.AnnotationVertxServiceDiscoverer;
import com.project5e.vertx.serviceproxy.service.VertxServiceBeanDefinitionRegistryPostProcessor;
import com.project5e.vertx.serviceproxy.service.VertxServiceDiscoverer;
import com.project5e.vertx.serviceproxy.servicefactory.ServiceProxyRegister;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHandler;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Vertx.class, ProxyHandler.class})
@AutoConfigureAfter(VertxAutoConfiguration.class)
public class VertxServiceAutoConfiguration {

    @ConditionalOnMissingBean
    @ConditionalOnBean(VerticleDiscoverer.class)
    @Bean
    public VertxServiceDiscoverer defaultVertxServiceDiscoverer(VerticleDiscoverer verticleDiscoverer) {
        return new AnnotationVertxServiceDiscoverer(verticleDiscoverer.findVerticles());
    }

//    @ConditionalOnMissingBean
//    @ConditionalOnBean({Vertx.class, VertxServiceDiscoverer.class})
//    @Bean
    public VertxServiceBeanDefinitionRegistryPostProcessor vertxServiceBeanDefinitionRegistryPostProcessor(
            Vertx vertx,
            VertxServiceDiscoverer discoverer
    ) {
        return new VertxServiceBeanDefinitionRegistryPostProcessor(vertx, discoverer);
    }

//    @ConditionalOnMissingBean
//    @ConditionalOnBean({Vertx.class, VertxServiceDiscoverer.class})
//    @Bean
//    public VertxServiceLifecycle vertxServiceLifecycle(final Vertx vertx, final VertxServiceDiscoverer discoverer) {
//        return new VertxServiceLifecycle(vertx, discoverer);
//    }

    @ConditionalOnMissingBean
    @ConditionalOnBean({Vertx.class, VertxServiceDiscoverer.class})
    @Bean
    public ServiceProxyRegister serviceProxyRegister(final Vertx vertx, final VertxServiceDiscoverer discoverer) {
        return new ServiceProxyRegister(vertx, discoverer);
    }

}
