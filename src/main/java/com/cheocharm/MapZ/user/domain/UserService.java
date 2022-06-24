package com.cheocharm.MapZ.user.domain;

import com.cheocharm.MapZ.common.exception.InvalidJwtException;
import com.cheocharm.MapZ.common.jwt.JwtCreateUtils;
import com.cheocharm.MapZ.common.oauth.OauthApi;
import com.cheocharm.MapZ.common.oauth.OauthUrl;
import com.cheocharm.MapZ.common.util.ObjectMapperUtils;
import com.cheocharm.MapZ.common.oauth.GoogleYml;
import com.cheocharm.MapZ.user.domain.dto.GoogleIdTokenDto;
import com.cheocharm.MapZ.user.domain.dto.UserLoginDto;
import com.cheocharm.MapZ.user.domain.dto.TokenPairResponseDto;
import com.cheocharm.MapZ.user.domain.dto.UserSignUpDto;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;

    private final JwtCreateUtils jwtCreateUtils;
    private final GoogleYml googleYml;
    private final OauthApi oauthApi;

    @Transactional
    public TokenPairResponseDto signUpGoogle(UserSignUpDto userSignUpDto) {

        ResponseEntity<String> response = oauthApi.callGoogle(OauthUrl.GOOGLE, userSignUpDto.getIdToken());

        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            throw new InvalidJwtException();
        }

        GoogleIdTokenDto idToken = checkAudAndGetTokenDto(response);

        Optional<UserEntity> findUser = userRepository.findByEmail(idToken.getEmail());

        if (findUser.isPresent()) {
            final UserEntity userEntity = findUser.get();
            final TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(idToken.getEmail(), userSignUpDto.getUsername());
            userEntity.updateRefreshToken(tokenPair.getRefreshToken());
            return tokenPair;
        }

        TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(idToken.getEmail(), userSignUpDto.getUsername());

        userRepository.save(
                UserEntity.builder()
                        .email(idToken.getEmail())
                        .username(userSignUpDto.getUsername())
                        .userImageUrl("")
                        .bio("자기소개를 입력해주세요")
                        .refreshToken(tokenPair.getRefreshToken())
                        .build()
        );

        return tokenPair;
    }

    @Transactional
    public TokenPairResponseDto loginGoogle(UserLoginDto userLoginDto) {
        ResponseEntity<String> response = oauthApi.callGoogle(OauthUrl.GOOGLE, userLoginDto.getIdToken());

        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            throw new InvalidJwtException();
        }
        GoogleIdTokenDto idToken = checkAudAndGetTokenDto(response);

        Optional<UserEntity> findUser = userRepository.findByEmail(idToken.getEmail());

        if (findUser.isPresent()) {
            final UserEntity userEntity = findUser.get();
            final TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(idToken.getEmail(), userEntity.getUsername());
            userEntity.updateRefreshToken(tokenPair.getRefreshToken());
            return tokenPair;
        }

        return jwtCreateUtils.createNullToken();
    }

    private GoogleIdTokenDto checkAudAndGetTokenDto(ResponseEntity<String> response) {
        try {
            GoogleIdTokenDto idToken = ObjectMapperUtils.getObjectMapper().readValue(response.getBody(), GoogleIdTokenDto.class);

            if (!idToken.getAud().equals(googleYml.getClient_id())) {
                throw new InvalidJwtException();
            }
            return idToken;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("readvalue 에러");
        }
    }
}
