package com.mapz.api.group.presentation.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
