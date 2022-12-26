package com.cheocharm.MapZ.group.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class ChangeChiefDto {

    @NotNull
    private Long groupId;

    @NotNull
    private Long userId;
}
