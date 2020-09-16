package com.project5e.vertx.core.autoconfigure;

import com.project5e.vertx.core.aop.VerticleAnnotationAdvisor;
import com.project5e.vertx.core.aop.VerticleAnnotationBeanPostProcessor;
import com.project5e.vertx.core.aop.VerticleAnnotationInterceptor;
import com.project5e.vertx.core.service.AnnotationVerticleDiscoverer;
import com.project5e.vertx.core.service.VerticleDiscoverer;
import com.project5e.vertx.core.servicefactory.VertxLifecycle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = true)
@ConditionalOnClass(Vertx.class)
@EnableConfigurationProperties(VertxProperties.class)
public class VertxAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public VerticleDiscoverer defaultVerticleDiscoverer() {
        return new AnnotationVerticleDiscoverer();
    }

    @ConditionalOnMissingBean
    @Bean
    public Vertx defaultVertx(final VertxProperties properties) {
        VertxOptions options = new VertxOptions();
        options.setWorkerPoolSize(properties.getWorkerPoolSize());
        return Vertx.vertx(options);
    }

//    @ConditionalOnMissingBean
//    @Bean
    public VerticleAnnotationAdvisor verticleAdvisor() {
        VerticleAnnotationInterceptor interceptor = new VerticleAnnotationInterceptor();
        return new VerticleAnnotationAdvisor(interceptor);
    }

    @ConditionalOnMissingBean
    @Bean
    public VerticleAnnotationBeanPostProcessor verticleAnnotationBeanPostProcessor() {
        return new VerticleAnnotationBeanPostProcessor(verticleAdvisor());
    }

    @ConditionalOnMissingBean
    @ConditionalOnBean({Vertx.class, VerticleDiscoverer.class})
    @Bean
    public VertxLifecycle vertxLifecycle(final Vertx vertx, final VerticleDiscoverer discoverer) {
        return new VertxLifecycle(vertx, discoverer);
    }

}
