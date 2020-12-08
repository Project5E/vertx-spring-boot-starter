package com.project5e.vertx.sample.router.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SearchParam {
    @NotNull
    @NotBlank
    private List<Integer> id;

    private String keyword;
    private String category;
    private Integer tagid;
    private List<String> locations;
    private Integer deleted;
    private String createdfrom;
    private String createdto;
    private String updatedfrom;
    private String updatedto;
    private Integer limit;
    private Integer offset;
    private List<String> sort;
    private Boolean sortonline;
}