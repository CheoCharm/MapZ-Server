package com.cheocharm.MapZ.group.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class SearchGroupDto {

    @NotNull
    private Integer page;

    @NotNull
    private String searchName;
}
