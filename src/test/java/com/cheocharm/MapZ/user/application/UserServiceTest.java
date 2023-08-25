package com.cheocharm.MapZ.user.application;

import com.cheocharm.MapZ.ServiceTest;
import com.cheocharm.MapZ.agreement.Agreement;
import com.cheocharm.MapZ.common.client.webclient.GoogleAuthWebClient;
import com.cheocharm.MapZ.common.exception.jwt.InvalidJwtException;
import com.cheocharm.MapZ.common.exception.user.DuplicatedEmailException;
import com.cheocharm.MapZ.common.exception.user.DuplicatedUsernameException;
import com.cheocharm.MapZ.common.exception.user.NotFoundUserException;
import com.cheocharm.MapZ.common.exception.user.PresentUserException;
import com.cheocharm.MapZ.common.exception.user.WrongPasswordException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.common.jwt.JwtCommonUtils;
import com.cheocharm.MapZ.common.jwt.JwtCreateUtils;
import com.cheocharm.MapZ.common.oauth.GoogleYml;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.user.domain.UserProvider;
import com.cheocharm.MapZ.user.presentation.dto.request.GoogleSignInRequest;
import com.cheocharm.MapZ.user.presentation.dto.request.GoogleSignUpRequest;
import com.cheocharm.MapZ.user.presentation.dto.request.MapZSignInRequest;
import com.cheocharm.MapZ.user.presentation.dto.request.MapZSignUpRequest;
import com.cheocharm.MapZ.user.presentation.dto.request.PasswordChangeRequest;
import com.cheocharm.MapZ.user.presentation.dto.response.GetUserListResponse;
import com.cheocharm.MapZ.user.presentation.dto.response.GoogleIdTokenResponse;
import com.cheocharm.MapZ.user.presentation.dto.response.MyPageInfoResponse;
import com.cheocharm.MapZ.user.presentation.dto.response.TokenPairResponse;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.cheocharm.MapZ.common.fixtures.UserFixtures.AGREE_PUSH_AlERT;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.DISAGREE_PUSH_AlERT;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.VALID_EMAIL;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.VALID_PASSWORD;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.VALID_USERNAME;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.WRONG_PASSWORD;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.createIdTokenResponse;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.createPasswordEncodedUser;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.createWrongIdTokenResponse;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.googleSignUpUser;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.mapZSignUpUser;
import static com.cheocharm.MapZ.common.fixtures.UserGroupFixtures.오픈된그룹_구글가입유저;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.cheocharm.MapZ.common.util.PagingUtils.USER_SIZE;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyDescPageConfigBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class UserServiceTest extends ServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private JwtCreateUtils jwtCreateUtils;

    @SpyBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private GoogleAuthWebClient googleAuthWebClient;

    @MockBean
    private JwtCommonUtils jwtCommonUtils;

    @Autowired
    private GoogleYml googleYml;

    @TestConfiguration
    static class MockitoMailSenderConfiguration {
        @Bean
        @Primary
        JavaMailSender mailSender() {
            return mock(JavaMailSender.class);
        }
    }

    private static EasyRandom easyRandom = new EasyRandom();
    private static final User MAPZ_USER = mapZSignUpUser();

    @Test
    @DisplayName("구글 회원가입")
    void signUpGoogle() {

        //given
        GoogleSignUpRequest request = new GoogleSignUpRequest(
                VALID_USERNAME, "idToken", true
        );
        GoogleIdTokenResponse idToken = createIdTokenResponse(googleYml.getClient_id());
        TokenPairResponse tokenPairResponse = createdExpectedTokenPairResponse(
                VALID_EMAIL, request.getUsername(), UserProvider.GOOGLE);

        given(googleAuthWebClient.getGoogleAuth(anyString()))
                .willReturn(idToken);
        given(userRepository.findByEmailAndUserProvider(anyString(), eq(UserProvider.GOOGLE)))
                .willReturn(Optional.empty());

        //when
        final TokenPairResponse response = userService.signUpGoogle(request, getMockMultipartFile("file"));

        //then
        assertThat(response).usingRecursiveComparison()
                .isEqualTo(tokenPairResponse);
        then(userRepository).should().save(any(User.class));
        then(agreementRepository).should().save(any(Agreement.class));
    }

    @Test
    @DisplayName("구글 회원가입 시 AUD 필드 인증에 실패하면 예외를 발생시킨다.")
    void throwExceptionAudIsDifferentWhenGoogleSignUp() {

        //given
        GoogleSignUpRequest request = new GoogleSignUpRequest(
                VALID_USERNAME, "idToken", AGREE_PUSH_AlERT
        );
        GoogleIdTokenResponse wrongIdToken = createWrongIdTokenResponse();
        given(googleAuthWebClient.getGoogleAuth(anyString()))
                .willReturn(wrongIdToken);

        //when,then
        assertThatThrownBy(()-> userService.signUpGoogle(request, getMockMultipartFile("file")))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    @DisplayName("구글 회원가입시 이미 존재하는 회원이면 예외를 발생시킨다.")
    void throwExceptionWhenPresentGoogleUser() {

        //given
        GoogleSignUpRequest request = new GoogleSignUpRequest(
                VALID_USERNAME, "idToken", AGREE_PUSH_AlERT
        );
        MultipartFile file = getMockMultipartFile("file");

        GoogleIdTokenResponse idToken = createIdTokenResponse(googleYml.getClient_id());

        given(googleAuthWebClient.getGoogleAuth(anyString()))
                .willReturn(idToken);
        given(userRepository.findByEmailAndUserProvider(anyString(), eq(UserProvider.GOOGLE)))
                .willReturn(Optional.of(googleSignUpUser()));

        //when, then
        assertThatThrownBy(() -> userService.signUpGoogle(request, file))
                .isInstanceOf(PresentUserException.class);
    }

    @Test
    @DisplayName("구글에 가입된 유저는 바로 로그인할 수 있다.")
    void signInGoogle() throws Exception{

        //given
        User user = googleSignUpUser();
        GoogleSignInRequest request = new GoogleSignInRequest("idToken");

        GoogleIdTokenResponse idToken = createIdTokenResponse(googleYml.getClient_id());
        TokenPairResponse tokenPairResponse = createdExpectedTokenPairResponse(
                VALID_EMAIL, user.getUsername(), user.getUserProvider());

        given(googleAuthWebClient.getGoogleAuth(anyString()))
                .willReturn(idToken);
        given(userRepository.findByEmailAndUserProvider(anyString(), eq(UserProvider.GOOGLE)))
                .willReturn(Optional.of(user));

        //when
        TokenPairResponse response = userService.signInGoogle(request);

        //then
        assertThat(response).usingRecursiveComparison()
                .isEqualTo(tokenPairResponse);
    }

    @Test
    @DisplayName("구글 로그인 시에 회원가입이 되어있지 않은 유저라면 NullTokenPair를 반환한다.")
    void returnNullTokenWhenNotSignUpGoogle() throws Exception {

        //given
        GoogleSignInRequest request = new GoogleSignInRequest("idToken");

        GoogleIdTokenResponse idToken = createIdTokenResponse(googleYml.getClient_id());

        given(googleAuthWebClient.getGoogleAuth(anyString()))
                .willReturn(idToken);
        given(userRepository.findByEmailAndUserProvider(anyString(), eq(UserProvider.GOOGLE)))
                .willReturn(Optional.empty());
        given(jwtCreateUtils.createNullToken())
                .willReturn(TokenPairResponse.builder().build());
        //when
        TokenPairResponse response = userService.signInGoogle(request);

        //then
        assertThat(response.getAccessToken()).isNull();
        assertThat(response.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("구글 로그인 가입시 Aud 필드 값이 다르면 예외를 발생시킨다.")
    void throwExceptionAudIsDifferentWhenGoogleSignIn() throws Exception{

        //given
        GoogleSignInRequest request = new GoogleSignInRequest("idToken");

        GoogleIdTokenResponse wrongIdToken = createWrongIdTokenResponse();
        given(googleAuthWebClient.getGoogleAuth(anyString()))
                .willReturn(wrongIdToken);

        //when, then
        assertThatThrownBy(() -> userService.signInGoogle(request))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    @DisplayName("맵지 회원가입")
    void signUpMapZ() {

        //given
        final MapZSignUpRequest mapZSignUpRequest = new MapZSignUpRequest(
                "mapz@gmail.com", "mapz1234", "최강맵지", true);
        given(userRepository.findByUsername(mapZSignUpRequest.getUsername()))
                .willReturn(Optional.empty());
        final MockMultipartFile mockMultipartFile = getMockMultipartFileHasContent("profile");
        final TokenPairResponse expectedResponse = createdExpectedTokenPairResponse(
                "mapz@gmail.com", mapZSignUpRequest.getUsername(), UserProvider.MAPZ);

        //when, then
        assertThat(userService.signUpMapZ(mapZSignUpRequest, Objects.requireNonNull(mockMultipartFile)))
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("맵지 회원가입시 중복된 유저네임을 사용할 수 없다.")
    void cannotUseDuplicatedUsernameWhenSignUpMapZ() {

        //given
        MapZSignUpRequest request = new MapZSignUpRequest(
                VALID_EMAIL, VALID_PASSWORD, VALID_USERNAME, DISAGREE_PUSH_AlERT
        );
        MultipartFile file = getMockMultipartFile("file");
        given(userRepository.findByUsername(anyString()))
                .willReturn(Optional.of(MAPZ_USER));

        //when, then
        assertThatThrownBy(() -> userService.signUpMapZ(request, file))
                .isInstanceOf(DuplicatedUsernameException.class);
    }

    @Test
    @DisplayName("맵지 로그인")
    void signInMapZ() {

        //given
        MapZSignInRequest request = new MapZSignInRequest(
                MAPZ_USER.getEmail(),
                MAPZ_USER.getPassword()
        );
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        given(userRepository.findByEmailAndUserProvider(anyString(), eq(UserProvider.MAPZ)))
                .willReturn(Optional.of(createPasswordEncodedUser(encodedPassword)));
        TokenPairResponse expectedTokenPairResponse = createdExpectedTokenPairResponse(
                MAPZ_USER.getEmail(), MAPZ_USER.getUsername(), UserProvider.MAPZ
        );

        //when, then
        assertThat(userService.signInMapZ(request))
                .usingRecursiveComparison()
                .isEqualTo(expectedTokenPairResponse);
    }

    @Test
    @DisplayName("MapZ로 로그인 시 비밀번호가 틀리면 예외가 발생한다.")
    void throwExceptionWhenSignInMapZWithWrongPassword() {

        //given
        MapZSignInRequest request = new MapZSignInRequest(
                MAPZ_USER.getEmail(),
                WRONG_PASSWORD
        );
        User encodedUser = createPasswordEncodedUser(passwordEncoder.encode(VALID_PASSWORD));
        given(userRepository.findByEmailAndUserProvider(anyString(), eq(UserProvider.MAPZ)))
                .willReturn(Optional.of(encodedUser));

        //when, then
        assertThatThrownBy(() -> userService.signInMapZ(request))
                .isInstanceOf(WrongPasswordException.class);
    }

    @Test
    @DisplayName("회원가입 시 이메일 인증이 이루어진다.")
    void authEmail() {

        //given
        given(userRepository.findByEmailAndUserProvider(anyString(), any()))
                .willReturn(Optional.empty());
        willDoNothing()
                .given(mailSender).send(any(SimpleMailMessage.class));

        //when
        String number = userService.authEmail(VALID_EMAIL);

        //then
        assertThat(number.length()).isEqualTo(4);
    }

    @Test
    @DisplayName("회원가입 시 이미 존재하는 이메일이면 예외가 발생한다.")
    void throwExceptionWhenAuthEmail() {

        //given
        given(userRepository.findByEmailAndUserProvider(anyString(), any()))
                .willReturn(Optional.of(MAPZ_USER));

        //when, then
        assertThatThrownBy(() -> userService.authEmail(VALID_EMAIL))
                .isInstanceOf(DuplicatedEmailException.class);
    }

    @Test
    @DisplayName("이메일 인증을 통해 비밀번호를 초기화 단계로 넘어갈 수 있다.")
    void findPassword() {

        //given
        User user = mapZSignUpUser();
        given(userRepository.findByEmailAndUserProvider(anyString(), eq(UserProvider.MAPZ)))
                .willReturn(Optional.of(user));
        willDoNothing()
                .given(mailSender).send(any(SimpleMailMessage.class));

        //when
        String number = userService.findPassword(user.getEmail());

        //then
        assertThat(number.length()).isEqualTo(4);
    }

    @Test
    @DisplayName("이메일을 통해 비밀번호를 찾을때 존재하지 않는 유저면 예외가 발생한다.")
    void throwNotFoundExceptionWhenFindPassword() {

        //given
        given(userRepository.findByEmailAndUserProvider(anyString(), any()))
                .willReturn(Optional.empty());

        //when, then
        assertThatThrownBy(() -> userService.findPassword(VALID_EMAIL))
                .isInstanceOf(NotFoundUserException.class);
    }

    @Test
    @DisplayName("비밀번호를 새로 설정한다.")
    void setNewPassword() {

        //given
        User user = mapZSignUpUser();
        PasswordChangeRequest request = new PasswordChangeRequest(user.getEmail(), user.getPassword());
        given(userRepository.findByEmailAndUserProvider(anyString(), eq(UserProvider.MAPZ)))
                .willReturn(Optional.of(user));

        //when
        userService.setNewPassword(request);

        //then
        assertThat(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .isTrue();
    }

    @Test
    @DisplayName("그룹 초대를 위한 유저 검색")
    void searchUser() {

        //given
        try (MockedStatic<UserThreadLocal> utl = mockStatic(UserThreadLocal.class)) {
            utl.when(UserThreadLocal::get).thenReturn(MAPZ_USER);
            SliceImpl<User> slice = new SliceImpl<>(
                    List.of(googleSignUpUser(), mapZSignUpUser()),
                    applyDescPageConfigBy(0, USER_SIZE, FIELD_CREATED_AT),
                    false
            );
            given(userRepository.fetchByUserAndSearchName(any(User.class), anyString(), anyLong(), any(Pageable.class)))
                    .willReturn(slice);
            given(userGroupRepository.findBySearchNameAndGroupId(anyString(), anyLong()))
                    .willReturn(
                            List.of(오픈된그룹_구글가입유저())
                    );

            //when
            GetUserListResponse response = userService.searchUser(
                    0, 0L, "search", 1L
            );

            //then
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getUserList().size()).isEqualTo(slice.getContent().size());
        }
    }

    @Test
    @DisplayName("유저는 내정보를 확인할 수 있다.")
    void getMyPageInfo() {

        //given
        try (MockedStatic<UserThreadLocal> utl = mockStatic(UserThreadLocal.class)) {
            utl.when(UserThreadLocal::get).thenReturn(MAPZ_USER);

            //when
            MyPageInfoResponse response = userService.getMyPageInfo();

            //then
            assertThat(response.getUsername()).isEqualTo(MAPZ_USER.getUsername());
            assertThat(response.getUserImageUrl()).isEqualTo(MAPZ_USER.getUserImageUrl());
        }
    }

    @Test
    @DisplayName("refreshToken을 통해 토큰을 재발급 받을 수 있다.")
    void createTokenPairByRefreshToken() {

        //given
        String refreshToken = "refresh";
        given(jwtCommonUtils.findUserByToken(refreshToken))
                .willReturn(MAPZ_USER);
        given(jwtCommonUtils.getKey())
                .willReturn("key");
        TokenPairResponse expectedTokenPairResponse = createdExpectedTokenPairResponse(
                MAPZ_USER.getEmail(), MAPZ_USER.getUsername(), MAPZ_USER.getUserProvider()
        );
        given(jwtCreateUtils.createTokenPair(anyString()))
                .willReturn(expectedTokenPairResponse);

        //when
        TokenPairResponse response = userService.refresh(refreshToken);

        //then
        assertThat(response).usingRecursiveComparison()
                .isEqualTo(expectedTokenPairResponse);
    }

    private TokenPairResponse createdExpectedTokenPairResponse(String email, String username, UserProvider provider) {
        final TokenPairResponse tokenPairResponse = new TokenPairResponse(
                "accessToken", "refreshToken");
        given(jwtCreateUtils.createTokenPair(anyString(), anyString(), any()))
                .willReturn(tokenPairResponse);
        return tokenPairResponse;
    }

}