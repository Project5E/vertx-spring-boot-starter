package com.project5e.vertx.web.ext;

public interface EnumParamConverter<E, T> {

    E convertToEnum(T t);

}
