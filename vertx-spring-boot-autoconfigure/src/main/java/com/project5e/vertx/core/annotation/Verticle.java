package com.project5e.vertx.core.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * 描述一个 Verticle 实例
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface Verticle {
}
