package com.cheocharm.MapZ.user.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class GoogleSignUpDto {

    @NotNull
    private String username;

    @NotNull
    private String idToken;
}
