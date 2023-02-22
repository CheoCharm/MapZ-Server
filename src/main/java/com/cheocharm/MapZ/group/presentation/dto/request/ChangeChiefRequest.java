package com.cheocharm.MapZ.group.presentation.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class ChangeChiefRequest {

    @NotNull
    private Long groupId;

    @NotNull
    private Long userId;
}
