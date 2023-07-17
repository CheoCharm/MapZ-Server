package com.cheocharm.MapZ.group.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
public class UpdateGroupRequest {
    @NotNull
    private Long groupId;

    @NotNull
    private String groupName;

    @NotNull
    private String bio;

    @NotNull
    private Boolean changeStatus;
}
