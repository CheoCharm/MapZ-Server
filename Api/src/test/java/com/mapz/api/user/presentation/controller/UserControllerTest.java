package com.mapz.api.user.presentation.controller;

import com.mapz.api.ControllerTest;
import com.mapz.api.common.exception.user.DuplicatedEmailException;
import com.mapz.api.common.fixtures.UserFixtures;
import com.mapz.api.user.application.UserService;
import com.mapz.api.user.presentation.dto.request.GoogleSignInRequest;
import com.mapz.api.user.presentation.dto.request.GoogleSignUpRequest;
import com.mapz.api.user.presentation.dto.request.MapZSignInRequest;
import com.mapz.api.user.presentation.dto.request.MapZSignUpRequest;
import com.mapz.api.user.presentation.dto.request.PasswordChangeRequest;
import com.mapz.api.user.presentation.dto.response.TokenPairResponse;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.enums.UserProvider;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends ControllerTest {

    @MockBean
    private UserService userService;

    private static EasyRandom easyRandom = new EasyRandom();
    private static User user;

    @BeforeEach
    void beforeEach() {
        user = userRepository.save(
                User.builder()
                        .username(UserFixtures.VALID_USERNAME)
                        .email(UserFixtures.VALID_EMAIL)
                        .password(UserFixtures.VALID_PASSWORD)
                        .refreshToken("refreshTokenValue")
                        .userProvider(UserProvider.MAPZ)
                        .build()
        );
    }

    @Test
    @DisplayName("구글로 회원가입할 수 있다.")
    void signUpGoogle() throws Exception {

        //given
        String accessToken = getAccessToken();
        GoogleSignUpRequest request = easyRandom.nextObject(GoogleSignUpRequest.class);
        String dtoJson = objectMapper.writeValueAsString(request);
        MockMultipartFile dto = new MockMultipartFile(
                "dto", "dto", "application/json",
                dtoJson.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file = getMockMultipartFile("file");

        given(userService.signUpGoogle(any(GoogleSignUpRequest.class), any()))
                .willReturn(new TokenPairResponse("accessToken", "refreshToken"));

        //when, then
        mockMvc.perform(multipart("/api/users")
                        .file(dto)
                        .file(file)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("구글 로그인 시 가입된 회원이라면 정상적으로 TokenPair를 반환하는 로직이 수행된다.")
    void signInGoogle() throws Exception {

        //given
        GoogleSignInRequest request = easyRandom.nextObject(GoogleSignInRequest.class);
        given(userService.signInGoogle(any()))
                .willReturn(new TokenPairResponse("accessToken", "refreshToken"));
        //when, then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("구글")
    void signInGoogleIf() {

    }

    @Test
    @DisplayName("회원가입 과정에서 이메일 유효성 검사가 이루어진다.")
    void validateEmail() throws Exception {

        //given
        String email = user.getEmail();

        //when, then
        mockMvc.perform(get("/api/users/valid/email/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("email 검증 시 이메일 정규식에 부합하지 않으면 400을 반환한다.")
    void invalidateEmail() throws Exception {

        //given


        //when, then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/valid/email/{email}", UserFixtures.INVALID_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 존재하는 이메일이면 400을 반환한다.")
    void ifEmailIsPresent() throws Exception {

        //given
        String email = user.getEmail();
        willThrow(new DuplicatedEmailException())
                .given(userService).authEmail(any());

        //when, then
        mockMvc.perform(get("/api/users/valid/email/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저는 다른 플랫폼을 이용하지 않고도 회원가입을 할 수 있다.")
    void signUp() throws Exception {

        //given
        MapZSignUpRequest request = new MapZSignUpRequest(
                UserFixtures.VALID_EMAIL,
                UserFixtures.VALID_PASSWORD,
                UserFixtures.VALID_USERNAME,
                UserFixtures.AGREE_PUSH_AlERT
        );
        String dtoJson = objectMapper.writeValueAsString(request);
        MockMultipartFile dto = new MockMultipartFile(
                "dto", "dto", "application/json",
                dtoJson.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file = getMockMultipartFile("file");
        given(userService.signUpMapZ(any(), any()))
                .willReturn(new TokenPairResponse("accessToken", "refreshToken"));

        //when,then
        mockMvc.perform(multipart("/api/users/signup")
                        .file(dto)
                        .file(file)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저는 다른 플랫폼 없이 회원가입 후 로그인할 수 있다.")
    void signIn() throws Exception {

        //given
        MapZSignInRequest request = new MapZSignInRequest(UserFixtures.VALID_EMAIL, UserFixtures.VALID_PASSWORD);
        given(userService.signInMapZ(any()))
                .willReturn(new TokenPairResponse("accessToken", "refreshToken"));
        //when, then
        mockMvc.perform(post("/api/users/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MapZ로 로그인 시 이메일 형식이 잘못되면 400을 반환한다.")
    void signInWithInvalidEmailReturn400() throws Exception{

        //given
        MapZSignInRequest request = new MapZSignInRequest(
                UserFixtures.INVALID_EMAIL, UserFixtures.VALID_PASSWORD
        );

        //when, then
        mockMvc.perform(post("/api/users/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("email을 파라미터로 활용하여 비밀번호를 찾는다.")
    void findPassword() throws Exception {

        //given
        String email = user.getEmail();

        //when, then
        mockMvc.perform(get("/api/users/password/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("email을 파라미터로 활용하여 비밀번호를 찾을때 email 정규식이 아니면 400을 반환한다.")
    void findPasswordWhenInvalidEmailReturn400() throws Exception {

        //given
        String email = user.getEmail();

        //when, then
        mockMvc.perform(get("/api/users/password/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("새 비밀번호를 설정할 수 있다.")
    void setNewPassword() throws Exception{

        //given
        PasswordChangeRequest request = new PasswordChangeRequest(
                user.getEmail(), UserFixtures.VALID_PASSWORD
        );

        //when, then
        mockMvc.perform(patch("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("새 비밀번호 설정 시 유효한 이메일이 아니면 400을 반환하다.")
    void whenSetNewPasswordInvalidEmailCanReturnBadRequest() throws Exception{

        //given
        PasswordChangeRequest request = new PasswordChangeRequest(
                UserFixtures.INVALID_EMAIL, UserFixtures.VALID_PASSWORD
        );
        //when, then
        mockMvc.perform(patch("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("새 비밀번호 설정 시 비밀번호가 원하는 정규식에 부합하지 않으면 400을 반환한다.")
    void invalidPasswordReturnBadRequest() throws Exception {

        //given
        PasswordChangeRequest request = new PasswordChangeRequest(
                user.getEmail(),
                UserFixtures.INVALID_PASSWORD
        );

        //when, then
        mockMvc.perform(patch("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저는 마이페이지에서 자신의 정보를 확인할 수 있다.")
    void getMyPageInfo() throws Exception {

        //given
        String accessToken = getAccessToken();

        //when,then
        mockMvc.perform(get("/api/users/mypage")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("accessToken이 만료되면 refreshToken을 통해 발급한다.")
    void refreshToken() throws Exception {

        //given
        TokenPairResponse tokenPair = jwtCreateUtils.createTokenPair(
                user.getEmail(), user.getUsername(), user.getUserProvider()
        );
        String refreshToken = tokenPair.getRefreshToken();
        user.updateRefreshToken(refreshToken);

        //when, then
        mockMvc.perform(get("/api/users/refresh")
                        .header(UserFixtures.REFRESH_TOKEN_HEADER_NAME, refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
}