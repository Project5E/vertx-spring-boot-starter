package com.project5e.vertx.web.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = HttpMethod.GET)
public @interface GetMapping {

    String name() default "";

    String[] value() default {};

    String[] path() default {};

}
