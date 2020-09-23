package com.project5e.vertx.web.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = HttpMethod.PATCH)
public @interface PatchMapping {

    String name() default "";

    String[] value() default {};

    String[] path() default {};

}
