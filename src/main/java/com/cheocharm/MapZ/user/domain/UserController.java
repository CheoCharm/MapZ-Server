package com.cheocharm.MapZ.user.domain;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.user.domain.dto.UserLoginDto;
import com.cheocharm.MapZ.user.domain.dto.TokenPairResponseDto;
import com.cheocharm.MapZ.user.domain.dto.UserSignUpDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "UserController")
@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {

    private final UserService userService;

    @Operation(description = "구글회원가입")
    @PostMapping
    public CommonResponse<TokenPairResponseDto> googleSignUp(@Parameter @RequestBody @Valid UserSignUpDto userSignUpDto) {
        return CommonResponse.success(userService.signUpGoogle(userSignUpDto));
    }

    @Operation(description = "구글로그인")
    @PostMapping("/login")
    public CommonResponse<TokenPairResponseDto> googleLogin(@Parameter @RequestBody UserLoginDto userLoginDto) {
        return CommonResponse.success(userService.loginGoogle(userLoginDto));
    }

}
