package com.project5e.vertx.core.annotation;

import java.lang.annotation.*;

/**
 * TODO
 * 运行 vertx 的阻塞代码
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExecuteBlocking {
}
