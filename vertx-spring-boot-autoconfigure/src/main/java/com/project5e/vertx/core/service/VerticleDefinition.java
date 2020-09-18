package com.project5e.vertx.core.service;

import io.vertx.core.Verticle;
import lombok.Data;
import org.springframework.core.Ordered;

@Data
public class VerticleDefinition {

    /**
     * 在 spring 中的 beanName
     */
    private String beanName;

    /**
     * verticle 对象
     */
    private Verticle verticle;

    /**
     * verticle 代理对象
     */
    private Verticle verticleProxy;

    /**
     * verticle 目标类型
     */
    private Class<? extends Verticle> targetClass;

    /**
     * 启动顺序
     */
    private int order = Ordered.HIGHEST_PRECEDENCE;

}
