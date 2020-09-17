package com.project5e.vertx.serviceproxy.service;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

@Slf4j
public class VertxServiceBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private final Vertx vertx;
    private final VertxServiceDiscoverer discoverer;

    public VertxServiceBeanDefinitionRegistryPostProcessor(Vertx vertx, VertxServiceDiscoverer discoverer) {
        this.vertx = vertx;
        this.discoverer = discoverer;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        log.info("register personManager1>>>>>>>>>>>>>>>>");
        String proxyClassName = "com.project5e.vertx.sample.service.IPlusService" + "VertxEBProxy";
        BeanDefinition vertxEBProxyBeanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(proxyClassName)
                .addPropertyReference("plusServiceVertxEBProxy", "plusServiceVertxEBProxy")
                .addConstructorArgValue(vertx)
                .addConstructorArgValue("plus.bus")
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .getRawBeanDefinition();
        registry.registerBeanDefinition("plusServiceVertxEBProxy", vertxEBProxyBeanDefinition);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

}
