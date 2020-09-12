package com.project5e.vertx.core.service;

import com.project5e.vertx.core.annotation.Verticle;
import com.project5e.vertx.core.annotation.VertxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;

@Slf4j
public class AnnotationVertxServiceDiscoverer implements ApplicationContextAware, VertxServiceDiscoverer {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Collection<VerticleDefinition> findVerticles() {
        Collection<String> beanNames = Arrays.asList(applicationContext.getBeanNamesForAnnotation(Verticle.class));
        List<VerticleDefinition> definitions = new ArrayList<>(beanNames.size());
        for (String beanName : beanNames) {
            io.vertx.core.Verticle verticle = applicationContext.getBean(beanName, io.vertx.core.Verticle.class);
            Verticle verticleAnnotation = applicationContext.findAnnotationOnBean(beanName, Verticle.class);
            log.info("find verticle " + verticle.getClass().getCanonicalName());
            VerticleDefinition definition = new VerticleDefinition();
            definition.setBeanName(beanName);
            definition.setVerticle(verticle);
            definitions.add(definition);
        }
        return definitions;
    }

    @Override
    public Collection<VertxServiceDefinition> findVertxServices() {
        Collection<String> beanNames = Arrays.asList(applicationContext.getBeanNamesForAnnotation(VertxService.class));
        List<VertxServiceDefinition> definitions = new ArrayList<>(beanNames.size());
        for (String beanName : beanNames) {
            Object vertxService = applicationContext.getBean(beanName);
            VertxService vertxServiceAnnotation = applicationContext.findAnnotationOnBean(beanName, VertxService.class);
            log.info(vertxService.getClass().getSimpleName() + ", " + vertxServiceAnnotation.value());
        }
        return definitions;
    }

}
