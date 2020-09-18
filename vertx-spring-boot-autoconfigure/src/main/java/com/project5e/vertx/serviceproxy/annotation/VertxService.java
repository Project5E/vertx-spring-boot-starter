package com.project5e.vertx.serviceproxy.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * 描述一个 Service 实例
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface VertxService {

    String address();

    Class<?> register();

}
