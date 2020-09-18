package com.project5e.vertx.serviceproxy.service;

import com.project5e.vertx.core.service.VerticleDefinition;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AnnotationVertxServiceDiscoverer implements ApplicationContextAware, VertxServiceDiscoverer {

    private final Collection<VerticleDefinition> verticleDefinitions;
    private ApplicationContext applicationContext;
    private List<VertxServiceDefinition> definitions;

    public AnnotationVertxServiceDiscoverer(Collection<VerticleDefinition> verticleDefinitions) {
        this.verticleDefinitions = verticleDefinitions;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public synchronized List<VertxServiceDefinition> findVertxServices() {
        if (definitions == null) {
            Collection<String> beanNames = Arrays.asList(applicationContext.getBeanNamesForAnnotation(VertxService.class));
            definitions = new ArrayList<>(beanNames.size());
            for (String beanName : beanNames) {
                Object vertxService = applicationContext.getBean(beanName);
                Class<?> iface = ClassUtils.getAllInterfacesAsSet(vertxService).stream()
                        .filter(i -> AnnotationUtils.getAnnotation(i, ProxyGen.class) != null)
                        .findFirst().orElse(null);
                if (iface == null) {
                    log.warn("class [{}] does not implement any interface annotated by @ProxyGen", beanName);
                    continue;
                }
                VertxService vertxServiceAnnotation = applicationContext.findAnnotationOnBean(beanName, VertxService.class);
                assert vertxServiceAnnotation != null;
                if (StringUtils.isBlank(vertxServiceAnnotation.address())) {
                    log.warn("class [{}] 's address is blank", beanName);
                    continue;
                }
                VerticleDefinition verticleDefinition = verticleDefinitions.stream().filter(definition ->
                        definition.getTargetClass() == vertxServiceAnnotation.register()
                ).findFirst().orElse(null);
                if (verticleDefinition == null) {
                    log.warn("class [{}] 's verticle definition not found", beanName);
                    continue;
                }
                log.info("find service [{}], address [{}], verticle [{}]",
                        vertxService.getClass().getSimpleName(),
                        vertxServiceAnnotation.address(),
                        vertxServiceAnnotation.register().getSimpleName()
                );
                VertxServiceDefinition definition = new VertxServiceDefinition();
                definition.setAddress(vertxServiceAnnotation.address());
                definition.setBeanName(beanName);
                definition.setService(vertxService);
                definition.setServiceInterface((Class<Object>) iface);
                definition.setVerticleDefinition(verticleDefinition);
                definitions.add(definition);
            }
        }
        return definitions;
    }

    @Override
    public List<VertxServiceDefinition> findVertxServices(Verticle verticle) {
        Collection<VertxServiceDefinition> vertxServices = findVertxServices();
        return vertxServices.stream()
                .filter(definition -> definition.getVerticleDefinition().getVerticle().equals(verticle))
                .collect(Collectors.toList());
    }

}
