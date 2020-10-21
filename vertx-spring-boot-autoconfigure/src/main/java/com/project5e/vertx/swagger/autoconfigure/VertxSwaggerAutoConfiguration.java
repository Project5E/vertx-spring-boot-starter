package com.project5e.vertx.swagger.autoconfigure;

import com.project5e.vertx.swagger.service.VertxSwaggerDefinition;
import com.project5e.vertx.swagger.servicefactory.VertxSwaggerGenerator;
import com.project5e.vertx.web.autoconfigure.VertxWebAutoConfiguration;
import com.project5e.vertx.web.service.ProcessResult;
import io.swagger.v3.oas.models.info.Info;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Vertx.class, ProcessResult.class, Router.class})
@AutoConfigureAfter(VertxWebAutoConfiguration.class)
public class VertxSwaggerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VertxSwaggerDefinition vertxSwaggerDefinition() {
        VertxSwaggerDefinition vertxSwaggerDefinition = new VertxSwaggerDefinition();
        vertxSwaggerDefinition.setInfo(new Info().title("默认标题").description("默认描述信息"));
        return vertxSwaggerDefinition;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({Router.class, ProcessResult.class, VertxSwaggerDefinition.class})
    public VertxSwaggerGenerator vertxSwaggerGenerator(
        Router router, ProcessResult processResult, VertxSwaggerDefinition vertxSwaggerDefinition
    ) {
        VertxSwaggerGenerator vertxSwaggerGenerator = new VertxSwaggerGenerator(processResult, router, vertxSwaggerDefinition);
        vertxSwaggerGenerator.generateAndMount();
        return vertxSwaggerGenerator;
    }

}
