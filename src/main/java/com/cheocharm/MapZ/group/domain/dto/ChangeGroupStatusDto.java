package com.cheocharm.MapZ.group.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class ChangeGroupStatusDto {
    @NotNull
    private String group;

    @NotNull
    private Boolean changeStatus;

}
