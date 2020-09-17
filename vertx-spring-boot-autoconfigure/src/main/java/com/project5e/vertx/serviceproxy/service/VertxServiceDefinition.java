package com.project5e.vertx.serviceproxy.service;

import com.project5e.vertx.core.service.VerticleDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VertxServiceDefinition {

    private String beanName;

    /**
     * service bean 对象
     */
    private Object service;

    private String address;

    /**
     * service 实现的 @ProxyGen 接口
     */
    private Class<Object> serviceInterface;

    /**
     * 关联的 VerticleDefinition
     */
    private VerticleDefinition verticleDefinition;

}
