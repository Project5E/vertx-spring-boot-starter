package com.project5e.vertx.serviceproxy.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface VertxService {


    @AliasFor("address")
    String value() default "";

    @AliasFor("value")
    String address() default "";

    Class<?> register();

}
