package com.project5e.vertx.sample.router.dto;

import com.project5e.vertx.web.ext.EnumParamConverter;
import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum SomeType implements EnumParamConverter<SomeType, Integer> {

    RED(1),
    GREEN(2),

    ;

    private int code;

    @Override
    public SomeType convertToEnum(Integer integer) {
        return Arrays.stream(SomeType.values()).filter(someType -> someType.code == integer).findFirst().orElse(null);
    }
}
