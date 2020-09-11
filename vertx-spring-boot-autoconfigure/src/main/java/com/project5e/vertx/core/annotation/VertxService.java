package com.project5e.vertx.core.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface VertxService {


    @AliasFor(annotation = Service.class)
    String value();

    Class<?> register();

}
