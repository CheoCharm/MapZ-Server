package com.cheocharm.MapZ.group.presentation.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class ChangeInvitationStatusRequest {

    @NotNull
    private Long groupId;

    @NotNull
    private Boolean status;

    @NotNull
    private Long userId;
}
