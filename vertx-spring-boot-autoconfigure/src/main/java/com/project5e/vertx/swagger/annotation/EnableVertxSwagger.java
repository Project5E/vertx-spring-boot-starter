package com.project5e.vertx.swagger.annotation;

import com.project5e.vertx.swagger.autoconfigure.VertxSwaggerAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(VertxSwaggerAutoConfiguration.class)
public @interface EnableVertxSwagger {
}
