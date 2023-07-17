package com.cheocharm.MapZ.user.application;

import com.cheocharm.MapZ.ServiceTest;
import com.cheocharm.MapZ.common.jwt.JwtCreateUtils;
import com.cheocharm.MapZ.user.domain.UserProvider;
import com.cheocharm.MapZ.user.presentation.dto.request.GoogleSignUpRequest;
import com.cheocharm.MapZ.user.presentation.dto.request.MapZSignUpRequest;
import com.cheocharm.MapZ.user.presentation.dto.response.TokenPairResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class UserServiceTest extends ServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private JwtCreateUtils jwtCreateUtils;

//    @Test
    @DisplayName("구글 회원가입")
    void signUpGoogle() {

        //given
        final GoogleSignUpRequest googleSignUpRequest = new GoogleSignUpRequest("구글로그인", "idToken", true);

        given(userRepository.findByUsername(googleSignUpRequest.getUsername()))
                .willReturn(Optional.empty());

        //when
        final TokenPairResponse tokenPairResponse = new TokenPairResponse("accessToken", "refreshToken");
        given(jwtCreateUtils.createTokenPair("test@gmail.com", googleSignUpRequest.getUsername(), UserProvider.GOOGLE))
                .willReturn(tokenPairResponse);

        //then
        final TokenPairResponse tokenPairResponse1 = userService.signUpGoogle(googleSignUpRequest, null);
        assertThat(userService.signUpGoogle(googleSignUpRequest, null)).usingRecursiveComparison()
                .isEqualTo(tokenPairResponse);
    }

    @Test
    @DisplayName("맵지 회원가입")
    void signUpMapZ() {

        //given
        final MapZSignUpRequest mapZSignUpRequest = new MapZSignUpRequest("mapz@gmail.com", "mapz1234",
                "최강맵지", true);
        given(userRepository.findByUsername(mapZSignUpRequest.getUsername()))
                .willReturn(Optional.empty());

        //when
        final TokenPairResponse tokenPairResponse = new TokenPairResponse("accessToken", "refreshToken");
        given(jwtCreateUtils.createTokenPair("mapz@gmail.com", mapZSignUpRequest.getUsername(), UserProvider.MAPZ))
                .willReturn(tokenPairResponse);

        //then
        final MockMultipartFile mockMultipartFile = getMockMultipartFile("profile");

        assertThat(userService.signUpMapZ(mapZSignUpRequest, Objects.requireNonNull(mockMultipartFile)))
                .usingRecursiveComparison()
                .isEqualTo(tokenPairResponse);
    }

    @Test
    @DisplayName("그룹 초대를 위한 유저 검색")
    void searchUser() {

    }

}