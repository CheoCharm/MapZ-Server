package com.cheocharm.MapZ.user.domain;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.user.domain.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@Tag(name = "UserController")
@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {

    private final UserService userService;

    @Operation(description = "구글회원가입")
    @PostMapping
    public CommonResponse<TokenPairResponseDto> googleSignUp(@Parameter @RequestBody @Valid GoogleSignUpDto userSignUpDto) {
        return CommonResponse.success(userService.signUpGoogle(userSignUpDto));
    }

    @Operation(description = "구글로그인")
    @PostMapping("/login")
    public CommonResponse<TokenPairResponseDto> googleLogin(@Parameter @RequestBody @Valid GoogleLoginDto userLoginDto) {
        return CommonResponse.success(userService.loginGoogle(userLoginDto));
    }

    @Operation(description = "이메일 인증 및 비밀번호 유효성 검사")
    @PostMapping("/email")
    public CommonResponse<String> checkValidation(@Parameter @RequestBody @Valid CheckEmailPasswordDto checkEmailPasswordDto) {
        return CommonResponse.success(userService.sendEmail(checkEmailPasswordDto));
    }

    @Operation(description = "맵지회원가입")
    @PostMapping("/signup")
    public CommonResponse<TokenPairResponseDto> signUp(
            @Parameter @RequestPart(value = "dto") @Valid MapZSignUpDto mapZSignUpDto,
            @RequestPart(value = "file", required = false) MultipartFile multipartFile) throws IOException {
        return CommonResponse.success(userService.signUpMapZ(mapZSignUpDto, multipartFile));
    }

    @Operation(description = "맵지로그인")
    @PostMapping("/signin")
    public CommonResponse<TokenPairResponseDto> signIn(@Parameter @RequestBody @Valid MapZSignInDto mapZSignInDto) {
        return CommonResponse.success(userService.signInMapZ(mapZSignInDto));
    }

}
