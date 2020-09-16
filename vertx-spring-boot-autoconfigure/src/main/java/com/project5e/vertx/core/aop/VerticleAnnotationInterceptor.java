package com.project5e.vertx.core.aop;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

@Slf4j
public class VerticleAnnotationInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //获取目标类
        Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
        //获取指定方法
        Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
        //获取真正执行的方法,可能存在桥接方法
        final Method declaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        log.error(declaredMethod.toString());
        //获取返回类型
        Class<?> returnType = invocation.getMethod().getReturnType();
        //返回类型判断
//        if (User.class.isAssignableFrom(returnType)) {
//            return null;
//        }
        return invocation.proceed();
    }
}