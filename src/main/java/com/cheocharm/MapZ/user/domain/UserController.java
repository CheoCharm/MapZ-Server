package com.cheocharm.MapZ.user.domain;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.user.domain.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Tag(name = "UserController")
@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {

    private final UserService userService;

    @Operation(description = "구글회원가입")
    @PostMapping
    public CommonResponse<TokenPairResponseDto> googleSignUp(
            @Parameter @RequestPart(value = "dto") @Valid GoogleSignUpDto userSignUpDto,
            @RequestPart(value = "file", required = false) MultipartFile multipartFile) {
        return CommonResponse.success(userService.signUpGoogle(userSignUpDto, multipartFile));
    }

    @Operation(description = "구글로그인")
    @PostMapping("/login")
    public CommonResponse<TokenPairResponseDto> googleLogin(@Parameter @RequestBody @Valid GoogleLoginDto userLoginDto) {
        return CommonResponse.success(userService.loginGoogle(userLoginDto));
    }

    @Operation(description = "이메일 인증 유효성 검사")
    @GetMapping("/valid/email")
    public CommonResponse<String> checkEmail(@Parameter @RequestBody @Valid CheckEmailDto checkEmailDto) {
        return CommonResponse.success(userService.authEmail(checkEmailDto));
    }

    @Operation(description = "비밀번호 유효성 검사")
    @GetMapping("/valid/password")
    public CommonResponse<?> checkPassword(@Parameter @RequestBody @Valid CheckPasswordDto checkPasswordDto) {
        return CommonResponse.success();
    }

    @Operation(description = "맵지회원가입")
    @PostMapping("/signup")
    public CommonResponse<TokenPairResponseDto> signUp(
            @Parameter @RequestPart(value = "dto") @Valid MapZSignUpDto mapZSignUpDto,
            @RequestPart(value = "file", required = false) MultipartFile multipartFile) {
        return CommonResponse.success(userService.signUpMapZ(mapZSignUpDto, multipartFile));
    }

    @Operation(description = "맵지로그인")
    @PostMapping("/signin")
    public CommonResponse<TokenPairResponseDto> signIn(@Parameter @RequestBody @Valid MapZSignInDto mapZSignInDto) {
        return CommonResponse.success(userService.signInMapZ(mapZSignInDto));
    }

    @Operation(description = "비밀번호 찾기")
    @GetMapping("/password")
    public CommonResponse<String> findPassword(@Parameter @RequestBody @Valid FindPasswordDto findPasswordDto) {
        return CommonResponse.success(userService.findPassword(findPasswordDto));
    }

    @Operation(description = "새 비밀번호 설정")
    @PatchMapping("/password")
    public CommonResponse<?> setNewPassword(@Parameter @RequestBody @Valid GetNewPasswordDto getNewPasswordDto) {
        userService.setNewPassword(getNewPasswordDto);
        return CommonResponse.success();
    }

    @Operation(description = "유저 검색")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/user")
    public CommonResponse<?> searchUser(@RequestParam @Valid Integer page, @RequestParam @Valid String searchName) {
        return CommonResponse.success(userService.searchUser(page, searchName));
    }

}
