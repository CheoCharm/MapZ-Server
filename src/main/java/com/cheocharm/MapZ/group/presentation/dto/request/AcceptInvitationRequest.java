package com.cheocharm.MapZ.group.presentation.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class AcceptInvitationRequest {

    @NotNull
    private Long groupId;
}
