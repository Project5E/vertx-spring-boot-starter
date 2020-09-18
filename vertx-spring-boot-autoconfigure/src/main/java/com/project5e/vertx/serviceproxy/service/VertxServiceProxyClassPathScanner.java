package com.project5e.vertx.serviceproxy.service;

import com.project5e.vertx.core.annotation.Verticle;
import com.project5e.vertx.serviceproxy.annotation.VertxService;
import io.vertx.codegen.annotations.ProxyGen;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.HashMap;
import java.util.Map;

public class VertxServiceProxyClassPathScanner extends ClassPathBeanDefinitionScanner {

    private final AnnotationConfigApplicationContext applicationContext;

    private final Map<String, String> class2AddressMap;

    public VertxServiceProxyClassPathScanner(BeanDefinitionRegistry registry, AnnotationConfigApplicationContext applicationContext) throws ClassNotFoundException {
        super(registry);
        this.applicationContext = applicationContext;
        String[] verticleBeanNames = applicationContext.getBeanNamesForAnnotation(VertxService.class);
        class2AddressMap = new HashMap<>(verticleBeanNames.length);
        for (String beanName : verticleBeanNames) {
            BeanDefinition definition = this.applicationContext.getBeanDefinition(beanName);
            String beanClassName = definition.getBeanClassName();
            Class<?> serviceClass = ClassUtils.getClass(beanClassName);
            VertxService vertxServiceAnnotation = AnnotationUtils.getAnnotation(serviceClass, VertxService.class);
            Class<?> registerVerticleClass = vertxServiceAnnotation.register();
            String address = vertxServiceAnnotation.address();
            Class<?> proxyGenIService = findProxyGenIService(serviceClass);
            class2AddressMap.put(proxyGenIService.getCanonicalName() + "VertxEBProxy", address);
        }
    }

    private Class<?> findProxyGenIService(Class<?> serviceClass) {
        return ClassUtils.getAllInterfaces(serviceClass).stream().filter(aClass ->
                AnnotationUtils.getAnnotation(aClass, ProxyGen.class) != null
        ).findFirst().orElse(null);
    }

    @Override
    protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
        BeanDefinition beanDefinition = definitionHolder.getBeanDefinition();
        String beanClassName = beanDefinition.getBeanClassName();
        AbstractBeanDefinition newDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(beanClassName)
                .addConstructorArgReference("vertx")
                .addConstructorArgValue(class2AddressMap.get(beanClassName))
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .setPrimary(true)
                .getRawBeanDefinition();
        super.registerBeanDefinition(new BeanDefinitionHolder(newDefinition, definitionHolder.getBeanName()), registry);
    }
}