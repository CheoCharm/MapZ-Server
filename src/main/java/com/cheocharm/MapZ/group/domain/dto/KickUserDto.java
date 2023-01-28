package com.cheocharm.MapZ.group.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class KickUserDto {
    @NotNull
    private Long userId;

    @NotNull
    private Long groupId;
}
