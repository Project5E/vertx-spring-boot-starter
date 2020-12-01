package com.project5e.vertx.web.autoconfigure;

import com.project5e.vertx.web.service.HttpRouterGenerator;
import com.project5e.vertx.web.service.ProcessResult;
import com.project5e.vertx.web.service.WebAnnotationProcessor;
import com.project5e.vertx.web.servicefactory.VertxWebLifecycle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass({Vertx.class, Router.class})
@EnableConfigurationProperties(VertxWebProperties.class)
public class VertxWebAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public WebAnnotationProcessor webAnnotationProcessor() {
        return new WebAnnotationProcessor();
    }

    @ConditionalOnMissingBean
    @ConditionalOnBean(WebAnnotationProcessor.class)
    @Bean
    public ProcessResult processResult(WebAnnotationProcessor webAnnotationProcessor) {
        return webAnnotationProcessor.process();
    }

    @ConditionalOnBean({Vertx.class, ProcessResult.class})
    @ConditionalOnMissingBean
    @Bean
    public HttpRouterGenerator httpRouterGenerator(Vertx vertx, ProcessResult processResult, VertxWebProperties properties) {
        return new HttpRouterGenerator(vertx, processResult, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({HttpRouterGenerator.class})
    public Router router(HttpRouterGenerator generator) {
        return generator.generate();
    }

    @ConditionalOnBean({Vertx.class, Router.class})
    @ConditionalOnMissingBean
    @Bean
    public VertxWebLifecycle vertxWebLifecycle(Vertx vertx, Router router, VertxWebProperties vertxWebProperties) {
        return new VertxWebLifecycle(vertx, router, vertxWebProperties);
    }

}
