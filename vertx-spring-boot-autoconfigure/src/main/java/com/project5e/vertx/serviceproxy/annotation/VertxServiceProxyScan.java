package com.project5e.vertx.serviceproxy.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface VertxServiceProxyScan {

    String address();

    Class<?> register();

}
