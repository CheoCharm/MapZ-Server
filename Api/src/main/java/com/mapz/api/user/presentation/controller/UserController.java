package com.mapz.api.user.presentation.controller;

import com.mapz.api.common.CommonResponse;
import com.mapz.api.user.application.UserService;
import com.mapz.api.user.presentation.dto.request.GoogleSignInRequest;
import com.mapz.api.user.presentation.dto.request.GoogleSignUpRequest;
import com.mapz.api.user.presentation.dto.request.MapZSignInRequest;
import com.mapz.api.user.presentation.dto.request.MapZSignUpRequest;
import com.mapz.api.user.presentation.dto.request.PasswordChangeRequest;
import com.mapz.api.user.presentation.dto.response.GetUserListResponse;
import com.mapz.api.user.presentation.dto.response.MyPageInfoResponse;
import com.mapz.api.user.presentation.dto.response.TokenPairResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(description = "구글회원가입")
    @PostMapping
    public CommonResponse<TokenPairResponse> signUpGoogle(
            @Parameter @RequestPart(value = "dto") @Valid GoogleSignUpRequest userSignUpDto,
            @RequestPart(value = "file", required = false) MultipartFile multipartFile) {
        return CommonResponse.success(userService.signUpGoogle(userSignUpDto, multipartFile));
    }

    @Operation(description = "구글로그인")
    @PostMapping("/login")
    public CommonResponse<TokenPairResponse> signInGoogle(@Parameter @RequestBody @Valid GoogleSignInRequest userLoginDto) {
        return CommonResponse.success(userService.signInGoogle(userLoginDto));
    }

    @Operation(description = "이메일 인증 유효성 검사")
    @GetMapping("/valid/email/{email}")
    @Parameter(name = "email", in = ParameterIn.PATH, required = true)
    public CommonResponse<String> checkEmail(@PathVariable("email") @Email String email) {
        return CommonResponse.success(userService.authEmail(email));
    }

    @Operation(description = "맵지회원가입")
    @PostMapping("/signup")
    public CommonResponse<TokenPairResponse> signUpMapZ(
            @Parameter @RequestPart(value = "dto") @Valid MapZSignUpRequest mapZSignUpRequest,
            @RequestPart(value = "file", required = false) MultipartFile multipartFile) {
        return CommonResponse.success(userService.signUpMapZ(mapZSignUpRequest, multipartFile));
    }

    @Operation(description = "맵지로그인")
    @PostMapping("/signin")
    public CommonResponse<TokenPairResponse> signInMapZ(@Parameter @RequestBody @Valid MapZSignInRequest mapZSignInRequest) {
        return CommonResponse.success(userService.signInMapZ(mapZSignInRequest));
    }

    @Operation(description = "비밀번호 찾기")
    @GetMapping("/password/{email}")
    public CommonResponse<String> findPassword(@Parameter @PathVariable("email") @Email String email) {
        return CommonResponse.success(userService.findPassword(email));
    }

    @Operation(description = "새 비밀번호 설정")
    @PatchMapping("/password")
    public CommonResponse<?> setNewPassword(@Parameter @RequestBody @Valid PasswordChangeRequest passWordChangeRequest) {
        userService.setNewPassword(passWordChangeRequest);
        return CommonResponse.success();
    }

    @Operation(description = "그룹 초대를 위한 유저 검색")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/user")
    public CommonResponse<GetUserListResponse> searchUser(@RequestParam Integer page, @RequestParam Long cursorId, @RequestParam String searchName, @RequestParam Long groupId) {
        return CommonResponse.success(userService.searchUser(page, cursorId, searchName, groupId));
    }

    @Operation(description = "마이페이지 닉네임, 프로필 이미지 가져오기")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/mypage")
    public CommonResponse<MyPageInfoResponse> getMyPageInfo() {
        return CommonResponse.success(userService.getMyPageInfo());
    }

    @Operation(description = "accessToken 재발급")
    @Parameter(name = "refreshToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/refresh")
    public CommonResponse<TokenPairResponse> refresh(@RequestHeader("refreshToken") String refreshToken) {
        return CommonResponse.success(userService.refresh(refreshToken));
    }
}
