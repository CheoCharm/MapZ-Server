package com.cheocharm.MapZ.group.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class JoinGroupDto {

    @NotNull
    private Long groupId;

}
