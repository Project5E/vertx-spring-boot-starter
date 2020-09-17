package com.project5e.vertx.serviceproxy.service;

import com.project5e.vertx.serviceproxy.annotation.VertxService;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Verticle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.util.*;

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
            Class<?> iface = ClassUtils.getAllInterfacesAsSet(vertxService).stream().filter(i -> {
                ProxyGen proxyGenAnnotation = AnnotationUtils.getAnnotation(i, ProxyGen.class);
                return proxyGenAnnotation != null;
            }).findFirst().orElse(null);
            if (iface == null) {
                continue;
            }
            VertxService vertxServiceAnnotation = applicationContext.findAnnotationOnBean(beanName, VertxService.class);
            if (StringUtils.isBlank(vertxServiceAnnotation.address())) {
                continue;
            }
            VertxServiceDefinition definition = new VertxServiceDefinition();
            log.info(vertxService.getClass().getSimpleName() + ", " + vertxServiceAnnotation.address());
            definition.setAddress(vertxServiceAnnotation.address());
//            definition.setVerticle((Verticle) applicationContext.getBean(vertxServiceAnnotation.register()));
            definition.setVerticle((Verticle) applicationContext.getBean("numberVerticle"));
            definition.setService(vertxService);
            definition.setServiceInterface((Class<Object>) iface);
            definitions.add(definition);
        }
        return definitions;
    }
}
