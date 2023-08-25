package com.cheocharm.MapZ.user.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.*;

@AllArgsConstructor
@Getter
public class MapZSignUpRequest {
    @NotNull @Email
    private String email;

    @NotNull @Pattern(regexp = "^(?=.*\\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,20}",
            message = "비밀번호는 영문과 숫자 조합으로 8 ~ 20자리까지 가능합니다.")
    private String password;

    @NotNull @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,12}$" , message = "닉네임은 특수문자를 포함하지 않은 2~12자리여야 합니다.")
    private String username;

    @NotNull
    private Boolean pushAgreement;
}
