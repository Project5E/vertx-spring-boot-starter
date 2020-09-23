package com.project5e.vertx.web.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author leo
 * @date 2020/5/6 17:25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Component
public @interface Router {

    String name() default "";

    String value() default "/";

}
