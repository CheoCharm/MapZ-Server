package com.cheocharm.MapZ.group.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class ChangeGroupInfoDto {
    @NotNull
    private Long groupId;

    @NotNull
    private String groupName;

    @NotNull
    private String bio;

    @NotNull
    private Boolean changeStatus;

}
