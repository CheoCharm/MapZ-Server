package com.cheocharm.MapZ.group.presentation.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class KickUserRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long groupId;
}
