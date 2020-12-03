package com.project5e.vertx.web.service;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import com.project5e.vertx.web.annotation.*;
import com.project5e.vertx.web.exception.EmptyMethodsException;
import com.project5e.vertx.web.exception.EmptyPathsException;
import com.project5e.vertx.web.exception.IllegalPathException;
import com.project5e.vertx.web.intercepter.RouteInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author leo
 * @date 2020/5/6 17:35
 */
@Slf4j
public class WebAnnotationProcessor implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private ProcessResult result;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public synchronized ProcessResult process() {
        if (result == null) {
            result = new ProcessResult();
            // 收集所有的 路由
            String[] beanNames = applicationContext.getBeanNamesForAnnotation(Router.class);
            for (String beanName : beanNames) {
                Object bean = applicationContext.getBean(beanName);
                Class<?> clz = bean.getClass();
                RequestMapping requestMappingAnnotation = applicationContext.findAnnotationOnBean(beanName, RequestMapping.class);
                String[] rawParentPaths;
                if(requestMappingAnnotation != null && requestMappingAnnotation.value().length > 0){
                    rawParentPaths = requestMappingAnnotation.value();
                } else {
                    rawParentPaths = new String[]{"/"};
                }
                for (String rawParentPath : rawParentPaths) {
                    String parentPath = handlePath(rawParentPath);

                    Method[] methods = ClassUtil.getPublicMethods(clz);
                    RouterDescriptor routerDescriptor = new RouterDescriptor(clz, bean);
                    result.addRouterDescriptor(routerDescriptor);

                    for (Method method : methods) {
                        String[] value = getFieldValue(method, "value");
                        if (value == null) continue;
                        HttpMethod[] httpMethod = getFieldValue(method, "method");
                        String[] paths = Arrays.stream(value)
                            .map(s -> this.handlePath(parentPath + s))
                            .distinct()
                            .toArray(String[]::new);
                        if (paths.length == 0) {
                            throw new EmptyPathsException();
                        }
                        HttpMethod[] httpMethods = Arrays.stream(httpMethod).distinct().toArray(HttpMethod[]::new);
                        if (httpMethods.length == 0) {
                            throw new EmptyMethodsException();
                        }
                        MethodDescriptor methodDescriptor = new MethodDescriptor(routerDescriptor);
                        methodDescriptor.setPaths(paths);
                        methodDescriptor.setHttpMethods(httpMethods);
                        methodDescriptor.setMethod(method);
                        routerDescriptor.addMethodDescriptor(methodDescriptor);
                    }
                }
            }

            // 收集错误处理器
            beanNames = applicationContext.getBeanNamesForAnnotation(RouterAdvice.class);
            for (String beanName : beanNames) {
                Object bean = applicationContext.getBean(beanName);
                Class<?> clz = bean.getClass();
                Method[] publicMethods = ClassUtil.getPublicMethods(clz);
                for (Method method : publicMethods) {
                    ExceptionHandler handlerAnnotation = method.getAnnotation(ExceptionHandler.class);
                    if (handlerAnnotation == null) {
                        continue;
                    }
                    Class<? extends Throwable>[] careThrows = handlerAnnotation.value();
                    if (ArrayUtils.isEmpty(careThrows)) {
                        careThrows = new Class[]{Throwable.class};
                    }
                    result.addAdviceDescriptor(new RouterAdviceDescriptor(careThrows, bean, method));
                }
            }

            // 收集所有的拦截器
            beanNames = applicationContext.getBeanNamesForType(RouteInterceptor.class);
            for (String beanName : beanNames) {
                result.addRouteInterceptor((RouteInterceptor) applicationContext.getBean(beanName));
            }

        }
        return result;
    }

    private <T> T getFieldValue(Method method, String name) {
        T value = AnnotationUtil.getAnnotationValue(method, GetMapping.class, name);
        if (value == null) {
            value = AnnotationUtil.getAnnotationValue(method, PostMapping.class, name);
        }
        if (value == null) {
            value = AnnotationUtil.getAnnotationValue(method, PutMapping.class, name);
        }
        if (value == null) {
            value = AnnotationUtil.getAnnotationValue(method, PatchMapping.class, name);
        }
        if (value == null) {
            value = AnnotationUtil.getAnnotationValue(method, DeleteMapping.class, name);
        }
        if (value == null) {
            value = AnnotationUtil.getAnnotationValue(method, RequestMapping.class, name);
        }
        return value;
    }

    private String handlePath(String path) {
        path = path.trim();
        if (StringUtils.isBlank(path) || !path.startsWith("/")) {
            throw new IllegalPathException();
        }
        path = RegExUtils.replaceAll(path, "[/]+", "/");
        if (path.length() > 1) {
            path = StringUtils.stripEnd(path, "/");
        }
        return path;
    }


}
