package com.mapz.api.common.fixtures;

import com.mapz.api.user.presentation.dto.response.GoogleIdTokenResponse;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.enums.UserProvider;

public class UserFixtures {

    public static final String VALID_PASSWORD ="mapZPassword0606";
    public static final String INVALID_PASSWORD = "invalidPassword";
    public static final String WRONG_PASSWORD = "wrongPassword12";

    public static final String VALID_EMAIL = "mapzbest@gmail.com";
    public static final String INVALID_EMAIL = "wrongEmail";

    public static final String VALID_USERNAME = "최강맵지";
    public static final String GOOGLE_VALID_USERNAME = "구글로가입함";
    public static final String INVALID_USERNAME = "z!존맵지";

    public static final boolean AGREE_PUSH_AlERT = true;
    public static final boolean DISAGREE_PUSH_AlERT = false;

    public static final String REFRESH_TOKEN_HEADER_NAME = "refreshToken";

    public static final String USER_IMAGE_URL = "image1";

    public static final String DEFAULT_BIO = "자기소개를 입력해주세요.";

    public static User mapZSignUpUser() {
        return User.createUser(VALID_EMAIL, VALID_USERNAME, VALID_PASSWORD, DEFAULT_BIO,
                "refresh", UserProvider.MAPZ);
    }

    public static User googleSignUpUser() {
        return User.createUserNoPassword(VALID_EMAIL, GOOGLE_VALID_USERNAME, DEFAULT_BIO,
                "refresh", UserProvider.GOOGLE);
    }

    public static User createPasswordEncodedUser(String encodedPassword) {
        return new User(2L, VALID_EMAIL, VALID_USERNAME, encodedPassword, USER_IMAGE_URL, DEFAULT_BIO,
                "fcm", "refresh", UserProvider.MAPZ);
    }

    public static GoogleIdTokenResponse createIdTokenResponse(String googleId) {
        return GoogleIdTokenResponse.builder()
                .aud(googleId)
                .email(VALID_EMAIL)
                .build();
    }

    public static GoogleIdTokenResponse createWrongIdTokenResponse() {
        return GoogleIdTokenResponse.builder()
                .aud("wrongAud!!")
                .email(VALID_EMAIL)
                .build();
    }
}
