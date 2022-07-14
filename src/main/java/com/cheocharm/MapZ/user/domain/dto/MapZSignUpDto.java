package com.cheocharm.MapZ.user.domain.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;

@Getter
public class MapZSignUpDto {
    @NotNull
    String email;

    @NotNull
    String password;

    @NotNull @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,12}$" , message = "닉네임은 특수문자를 포함하지 않은 2~12자리여야 합니다.")
    String username;

    @NotNull
    Boolean pushAgreement;
}
