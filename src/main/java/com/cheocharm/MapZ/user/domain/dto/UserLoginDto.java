package com.cheocharm.MapZ.user.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class UserLoginDto {

    @NotNull
    private String idToken;
}
