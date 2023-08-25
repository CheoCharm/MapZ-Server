package com.cheocharm.MapZ.user.presentation.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
