package com.project5e.vertx.sample.router.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class Apple {

    @NotBlank
    private String color;
}
