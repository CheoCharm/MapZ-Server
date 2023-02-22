package com.cheocharm.MapZ.user.presentation.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
public class PasswordChangeRequest {
    @NotNull
    private String email;

    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,20}",
            message = "비밀번호는 영문과 숫자 조합으로 8 ~ 20자리까지 가능합니다.")
   private String password;
}
