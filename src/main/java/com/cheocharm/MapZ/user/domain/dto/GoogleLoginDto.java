package com.cheocharm.MapZ.user.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class GoogleLoginDto {

    @NotNull
    private String idToken;
}
