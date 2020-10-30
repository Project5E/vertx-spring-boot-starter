package com.project5e.vertx.sample.configuration;

import com.project5e.vertx.swagger.service.VertxSwaggerDefinition;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.CorsHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
public class SampleConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VertxSwaggerDefinition vertxSwaggerDefinition() {
        VertxSwaggerDefinition vertxSwaggerDefinition = new VertxSwaggerDefinition();
        vertxSwaggerDefinition.setInfo(
                new Info()
                        .title("亲子内容版")
                        .description("亲子内容版API")
                        .version("1.0.0")
                        .contact(new Contact().name("Zhang Xiuyu").email("y@moumoux.com"))
        );
        vertxSwaggerDefinition.setExternalDocumentation(
                new ExternalDocumentation()
                        .url("http://swagger.io")
                        .description("Find out more about Swagger")
        );
        vertxSwaggerDefinition.setServers(Arrays.asList(
                new Server().url("https://apitest.hulaplanet.com/22p").description("测试服"),
                new Server().url("http://localhost:8088").description("本地服务")
        ));
        vertxSwaggerDefinition.setTags(Arrays.asList(
                new Tag().name("ttp").description("亲子内容相关信息接口"),
                new Tag().name("misc").description("各种单独的接口"),
                new Tag().name("comment").description("文章评论接口"),
                new Tag().name("notification").description("消息中心(通知中心)接口"),
                new Tag().name("label").description("标签接口"),
                new Tag().name("sms").description("短信相关接口"),
                new Tag().name("pushkit").description("推送通知相关接口"),
                new Tag().name("goods").description("商品相关接口")
        ));
        return vertxSwaggerDefinition;
    }

    @Bean
    public CorsHandler corsHandler() {
        HashSet<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);
        allowedMethods.add(HttpMethod.DELETE);

        HashSet<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");
        allowedHeaders.add("Token");

        return CorsHandler.create(".*")
                .allowedHeaders(allowedHeaders)
                .allowedMethods(allowedMethods)
                .allowCredentials(true);
    }

}
