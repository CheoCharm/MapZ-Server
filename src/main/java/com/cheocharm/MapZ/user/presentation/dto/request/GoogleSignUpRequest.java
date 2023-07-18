package com.cheocharm.MapZ.user.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
public class GoogleSignUpRequest {

    @NotNull
    private String username;

    @NotNull
    private String idToken;

    @NotNull
    private Boolean pushAgreement;
}
