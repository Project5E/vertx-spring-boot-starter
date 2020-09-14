package com.project5e.vertx.serviceproxy.service;

import com.project5e.vertx.serviceproxy.annotation.VertxService;
import io.vertx.core.Verticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
public class AnnotationVertxServiceDiscoverer implements ApplicationContextAware, VertxServiceDiscoverer {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Collection<VertxServiceDefinition> findVertxServices() {
        Collection<String> beanNames = Arrays.asList(applicationContext.getBeanNamesForAnnotation(VertxService.class));
        List<VertxServiceDefinition> definitions = new ArrayList<>(beanNames.size());
        for (String beanName : beanNames) {
            Object vertxService = applicationContext.getBean(beanName);
            VertxService vertxServiceAnnotation = applicationContext.findAnnotationOnBean(beanName, VertxService.class);
            log.info(vertxService.getClass().getSimpleName() + ", " + vertxServiceAnnotation.address());
            VertxServiceDefinition definition = new VertxServiceDefinition();
            definition.setAddress(vertxServiceAnnotation.address());
            definition.setVerticle((Verticle) applicationContext.getBean(vertxServiceAnnotation.register()));
            definitions.add(definition);
        }
        return definitions;
    }
}
