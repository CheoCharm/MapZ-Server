package com.cheocharm.MapZ.group.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class ChangeInvitationStatusDto {

    @NotNull
    private Long groupId;

    @NotNull
    private Boolean status;

    @NotNull
    private Long userId;
}
