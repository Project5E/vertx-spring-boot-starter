package com.project5e.vertx.core.aop;

import com.project5e.vertx.core.annotation.Verticle;
import io.vertx.core.Promise;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

public class VerticleAnnotationAdvisor extends AbstractPointcutAdvisor {

    private final Advice advice;
    private final Pointcut pointcut;

    public VerticleAnnotationAdvisor(MethodInterceptor interceptor) {
        this.advice = interceptor;
        this.pointcut = buildPointcut();
    }

    private Pointcut buildPointcut() {
        //类级别
        Pointcut cpc = new AnnotationMatchingPointcut(Verticle.class, true);
        //方法级别
//        Pointcut mpc = new DynamicMethodMatcherPointcut() {
//            @Override
//            public boolean matches(Method method, Class<?> targetClass, Object... args) {
//                return method == ClassUtils.getMethod(targetClass, "start", Promise.class);
//            }
//        };
        //对于类和方法上都可以添加注解的情况
        //类上的注解,最终会将注解绑定到每个方法上
        ComposablePointcut result = new ComposablePointcut(cpc);
        return result;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
