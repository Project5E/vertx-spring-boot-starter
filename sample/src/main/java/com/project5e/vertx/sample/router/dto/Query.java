package com.project5e.vertx.sample.router.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class Query {

    @NotBlank
    private String requestId;
    @Min(12)
    private int page;
    @NotNull
    private int size;

    @Valid
    private List<Apple> apples;
}
