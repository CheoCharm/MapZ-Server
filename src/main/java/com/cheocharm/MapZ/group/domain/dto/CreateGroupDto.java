package com.cheocharm.MapZ.group.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
public class CreateGroupDto {

    @NotBlank
    private String groupName;

    @NotNull
    private String bio;

    @NotNull
    private Boolean changeStatus;
}
