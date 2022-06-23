package com.cheocharm.MapZ.user.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class UserSignUpDto {

    @NotNull
    private String username;

    @NotNull
    private String idToken;
}
