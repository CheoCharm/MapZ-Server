package com.cheocharm.MapZ.group.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
public class UpdateInvitationStatusRequest {

    @NotNull
    private Long groupId;

    @NotNull
    private Boolean status;

    @NotNull
    private Long userId;
}
