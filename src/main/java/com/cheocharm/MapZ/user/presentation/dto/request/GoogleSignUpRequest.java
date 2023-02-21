package com.cheocharm.MapZ.user.presentation.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class GoogleSignUpRequest {

    @NotNull
    private String username;

    @NotNull
    private String idToken;

    @NotNull
    private Boolean pushAgreement;
}
