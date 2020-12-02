package com.project5e.vertx.sample.configuration

import com.project5e.vertx.swagger.service.VertxSwaggerDefinition
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.handler.CorsHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class SampleConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun vertxSwaggerDefinition(): VertxSwaggerDefinition {
        val vertxSwaggerDefinition = VertxSwaggerDefinition()
        vertxSwaggerDefinition.info = Info()
                .title("亲子内容版")
                .description("亲子内容版API")
                .version("1.0.0")
                .contact(Contact().name("Zhang Xiuyu").email("y@moumoux.com"))
        vertxSwaggerDefinition.externalDocumentation = ExternalDocumentation()
                .url("http://swagger.io")
                .description("Find out more about Swagger")
        vertxSwaggerDefinition.servers = Arrays.asList(
                Server().url("https://apitest.hulaplanet.com/22p").description("测试服"),
                Server().url("http://localhost:8088").description("本地服务")
        )
        vertxSwaggerDefinition.tags = Arrays.asList(
                Tag().name("ttp").description("亲子内容相关信息接口"),
                Tag().name("misc").description("各种单独的接口"),
                Tag().name("comment").description("文章评论接口"),
                Tag().name("notification").description("消息中心(通知中心)接口"),
                Tag().name("label").description("标签接口"),
                Tag().name("sms").description("短信相关接口"),
                Tag().name("pushkit").description("推送通知相关接口"),
                Tag().name("goods").description("商品相关接口")
        )
        return vertxSwaggerDefinition
    }

    @Bean
    fun corsHandler(): CorsHandler {
        val allowedMethods = HashSet<HttpMethod>()
        allowedMethods.add(HttpMethod.GET)
        allowedMethods.add(HttpMethod.POST)
        allowedMethods.add(HttpMethod.OPTIONS)
        allowedMethods.add(HttpMethod.DELETE)
        val allowedHeaders = HashSet<String>()
        allowedHeaders.add("x-requested-with")
        allowedHeaders.add("Access-Control-Allow-Origin")
        allowedHeaders.add("origin")
        allowedHeaders.add("Content-Type")
        allowedHeaders.add("accept")
        allowedHeaders.add("Token")
        return CorsHandler.create(".*")
                .allowedHeaders(allowedHeaders)
                .allowedMethods(allowedMethods)
                .allowCredentials(true)
    }
}