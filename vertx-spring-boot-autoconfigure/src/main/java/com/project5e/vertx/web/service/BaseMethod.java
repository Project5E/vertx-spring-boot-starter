package com.project5e.vertx.web.service;

import io.vertx.core.Promise;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Data
public class BaseMethod {

    private BaseMethodType methodType;

    // get from reflect
    private Method method;
    private Parameter[] parameters;
    private Parameter resultParam;
    private Type resultParamType; //Promise<T>
    private Type returnType; // Future<T>
    private Type actualType; // T

    public BaseMethod(Method method) {
        this.method = method;
        parameters = method.getParameters();
        findResultParameter();
        returnType = method.getGenericReturnType();
        findActualType();
    }

    private void findResultParameter() {
        for (Parameter parameter : parameters) {
            if (parameter.getType() == Promise.class) {
                // 结果将放在参数 Promise 中
                resultParam = parameter;
                resultParamType = parameter.getParameterizedType();
                methodType = BaseMethodType.PARAM_RESULT;
                return;
            }
        }
        methodType = BaseMethodType.RETURN_RESULT;
    }

    // 只取第一个泛型
    private void findActualType() {
        Type type;
        switch (methodType) {
            case PARAM_RESULT:
                type = resultParamType;
                break;
            case RETURN_RESULT:
                type = returnType;
                break;
            default:
                return;
        }
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                actualType = actualTypeArguments[0];
            }
        }
    }

}
