package com.project5e.vertx.core.aop;

import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class VerticleAnnotationInterceptor implements MethodInterceptor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //获取目标类
        Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
        //获取指定方法
        Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
        //获取真正执行的方法,可能存在桥接方法
        final Method declaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        log.error(declaredMethod.toString());
        if (declaredMethod.getName().equals("start")) {
            Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0].equals(Promise.class)) {
                Map<String, BeforeStart> beansOfType = applicationContext.getBeansOfType(BeforeStart.class);
                beansOfType.forEach((s, beforeStart) -> {
                    beforeStart.doBeforeStart();
                });
            }
        }
        //获取返回类型
//        Class<?> returnType = invocation.getMethod().getReturnType();
        return invocation.proceed();
    }

}