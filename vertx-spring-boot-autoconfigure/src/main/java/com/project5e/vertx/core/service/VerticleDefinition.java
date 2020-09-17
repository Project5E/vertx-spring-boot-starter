package com.project5e.vertx.core.service;

import io.vertx.core.Verticle;
import lombok.Data;

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

}
