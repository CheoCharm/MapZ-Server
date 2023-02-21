package com.cheocharm.MapZ.group.presentation.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class ChangeGroupInfoRequest {
    @NotNull
    private Long groupId;

    @NotNull
    private String groupName;

    @NotNull
    private String bio;

    @NotNull
    private Boolean changeStatus;

}
