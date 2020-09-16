package com.project5e.vertx.core.aop;

import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor;
import org.springframework.beans.factory.BeanFactory;

public class VerticleAnnotationBeanPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor {

    public VerticleAnnotationBeanPostProcessor(VerticleAnnotationAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
    }
}
