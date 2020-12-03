package com.project5e.vertx.web.component;

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
    private boolean wrapped; // ResponseEntity wrapped

    public BaseMethod(Method method) {
        this.method = method;
        parameters = method.getParameters();
        returnType = method.getGenericReturnType();
        checkReturnLocation();
        findActualType();
    }

    /**
     * 以 return 为主
     */
    private void checkReturnLocation() {
        // TODO 这里应该就判断是不是 Future 类型
        if (!returnType.equals(Void.TYPE)) {
            methodType = BaseMethodType.RETURN_RESULT;
            return;
        }
        for (Parameter parameter : parameters) {
            if (parameter.getType() == Promise.class) {
                // 结果将放在参数 Promise 中
                resultParam = parameter;
                resultParamType = parameter.getParameterizedType();
                methodType = BaseMethodType.PARAM_RESULT;
                return;
            }
        }
    }

    // 只取第一个泛型
    private void findActualType() {
        if (methodType == null) {
            // 可能是错误处理器的方法
            return;
        }
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
        actualType = getFirstType(type);
        if (actualType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) actualType).getRawType();
            if (rawType == ResponseEntity.class) {
                wrapped = true;
                actualType = getFirstType(actualType);
            }
        }
    }

    private Type getFirstType(Type type) {
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                return actualTypeArguments[0];
            }
        }
        return null;
    }

}
