package com.project5e.vertx.core.service;

import com.project5e.vertx.core.annotation.Verticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class AnnotationVerticleDiscoverer implements ApplicationContextAware, VerticleDiscoverer {

    private ApplicationContext applicationContext;
    private List<VerticleDefinition> definitions;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public synchronized List<VerticleDefinition> findVerticles() {
        if (definitions == null) {
            List<String> beanNames = Arrays.asList(applicationContext.getBeanNamesForAnnotation(Verticle.class));
            definitions = new ArrayList<>(beanNames.size());
            for (String beanName : beanNames) {
                io.vertx.core.Verticle verticleProxy = applicationContext.getBean(beanName, io.vertx.core.Verticle.class);
                io.vertx.core.Verticle verticle = (io.vertx.core.Verticle) AopProxyUtils.getSingletonTarget(verticleProxy);
                Verticle verticleAnnotation = applicationContext.findAnnotationOnBean(beanName, Verticle.class);
                assert verticle != null;
                log.info("find verticle [{}]", verticle.getClass().getCanonicalName());
                VerticleDefinition definition = new VerticleDefinition();
                definition.setBeanName(beanName);
                definition.setVerticleProxy(verticleProxy);
                definition.setVerticle(verticle);
                definition.setTargetClass((Class<? extends io.vertx.core.Verticle>) AopUtils.getTargetClass(verticleProxy));
                definitions.add(definition);
            }
        }
        return definitions;
    }

}
