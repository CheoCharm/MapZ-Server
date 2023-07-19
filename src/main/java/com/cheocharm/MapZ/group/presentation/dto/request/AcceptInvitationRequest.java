package com.cheocharm.MapZ.group.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
public class AcceptInvitationRequest {

    @NotNull
    private Long groupId;
}