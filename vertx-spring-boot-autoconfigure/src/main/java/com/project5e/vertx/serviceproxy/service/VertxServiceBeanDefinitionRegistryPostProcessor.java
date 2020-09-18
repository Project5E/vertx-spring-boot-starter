package com.project5e.vertx.serviceproxy.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class VertxServiceBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @SneakyThrows
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        log.info("register VertxEBProxy");
        ClassPathBeanDefinitionScanner scanner = new VertxServiceProxyClassPathScanner(registry, (AnnotationConfigApplicationContext) applicationContext);
        scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*VertxEBProxy")));
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        Class<?> appClass = beanMap.isEmpty() ? null : beanMap.values().toArray()[0].getClass();
        assert appClass != null;
        String packageName = ClassUtils.getPackageName(appClass);
        scanner.scan(packageName);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
