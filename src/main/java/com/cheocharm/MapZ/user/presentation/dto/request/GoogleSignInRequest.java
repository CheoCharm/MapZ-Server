package com.cheocharm.MapZ.user.presentation.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class GoogleSignInRequest {

    @NotNull
    private String idToken;
}
