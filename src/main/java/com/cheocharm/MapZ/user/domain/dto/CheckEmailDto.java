package com.cheocharm.MapZ.user.domain.dto;

import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
public class CheckEmailDto {
    @NotNull @Email
    String email;
}
