package com.project5e.vertx.swagger.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface SwaggerPage {

    String value() default "default";

}
