package com.cheocharm.MapZ.user.domain;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.common.jwt.JwtCreateUtils;
import com.cheocharm.MapZ.user.domain.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Email;

@Tag(name = "UserController")
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {

    private final UserService userService;
    private final JwtCreateUtils jwtCreateUtils;

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
    @GetMapping("/valid/email/{email}")
    @Parameter(name = "email", in = ParameterIn.PATH, required = true)
    public CommonResponse<String> checkEmail(@PathVariable("email") @Email String email) {
        return CommonResponse.success(userService.authEmail(email));
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
    @GetMapping("/password/{email}")
    public CommonResponse<String> findPassword(@Parameter @PathVariable("email") @Email String email) {
        return CommonResponse.success(userService.findPassword(email));
    }

    @Operation(description = "새 비밀번호 설정")
    @PatchMapping("/password")
    public CommonResponse<?> setNewPassword(@Parameter @RequestBody @Valid GetNewPasswordDto getNewPasswordDto) {
        userService.setNewPassword(getNewPasswordDto);
        return CommonResponse.success();
    }

    @Operation(description = "그룹 초대를 위한 유저 검색")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/user")
    public CommonResponse<GetUserListDto> searchUser(@RequestParam Integer page, @RequestParam String searchName, @RequestParam String groupName) {
        return CommonResponse.success(userService.searchUser(page, searchName, groupName));
    }

    @Operation(description = "마이페이지 닉네임, 프로필 이미지 가져오기")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/mypage")
    public CommonResponse<MyPageInfoDto> getMyPageInfo() {
        return CommonResponse.success(userService.getMyPageInfo());
    }

    @Operation(description = "accessToken 재발급")
    @Parameter(name = "refreshToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/refresh")
    public CommonResponse<TokenPairResponseDto> refresh(@RequestHeader("refreshToken") String refreshToken) {
        return CommonResponse.success(jwtCreateUtils.createAccessToken(refreshToken));
    }
}
