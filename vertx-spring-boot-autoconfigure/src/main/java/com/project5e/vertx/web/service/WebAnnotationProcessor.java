package com.project5e.vertx.web.service;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import com.project5e.vertx.web.annotation.*;
import com.project5e.vertx.web.exception.EmptyMethodsException;
import com.project5e.vertx.web.exception.EmptyPathsException;
import com.project5e.vertx.web.exception.IllegalPathException;
import com.project5e.vertx.web.exception.NewInstanceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author leo
 * @date 2020/5/6 17:35
 */
@Slf4j
public class WebAnnotationProcessor implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ProcessResult process() {
        ProcessResult result = new ProcessResult();
        Set<Class<?>> classes = applicationContext.getBeansWithAnnotation(Router.class).values()
            .stream().map(Object::getClass).collect(Collectors.toSet());

        for (Class<?> clz : classes) {
            Router routerAnnotation = AnnotationUtil.getAnnotation(clz, Router.class);
            String parentPath = handlePath(routerAnnotation.value());

            Method[] methods = ClassUtil.getPublicMethods(clz);
            Object routeInstance;
            try {
                routeInstance = clz.newInstance();
            } catch (Exception e) {
                throw new NewInstanceException(e);
            }
            RouterDescriptor routerDescriptor = new RouterDescriptor(clz, routeInstance);
            result.addRouterDescriptor(routerDescriptor);

            for (Method method : methods) {
                String[] value = getValues(method);
                if (value == null) continue;
                io.vertx.core.http.HttpMethod[] httpMethods = getHttpMethods(method);
                String[] paths = Arrays.stream(value)
                    .map(s -> this.handlePath(parentPath + s))
                    .distinct()
                    .toArray(String[]::new);
                if (paths.length == 0) {
                    throw new EmptyPathsException();
                }
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

        return result;
    }

    private String[] getValues(Method method) {
        String name = "value";
        String[] value = AnnotationUtil.getAnnotationValue(method, GetMapping.class, name);
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

    private io.vertx.core.http.HttpMethod[] getHttpMethods(Method method) {
        String name = "method";
        HttpMethod[] value = AnnotationUtil.getAnnotationValue(method, GetMapping.class, name);
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

        return Arrays.stream(value).distinct()
            .map(item -> io.vertx.core.http.HttpMethod.valueOf(item.name()))
            .toArray(io.vertx.core.http.HttpMethod[]::new);
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
