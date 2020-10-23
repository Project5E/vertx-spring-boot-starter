package com.project5e.vertx.web.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ExceptionHandler {

    Class<? extends Throwable>[] value() default {};

}
