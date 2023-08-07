package com.cheocharm.MapZ.user.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
public class MapZSignInRequest {
    @NotNull @Email
    private String email;

    @NotNull
    private String password;
}
