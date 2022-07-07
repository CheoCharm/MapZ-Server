package com.cheocharm.MapZ.user.domain;

import com.cheocharm.MapZ.common.exception.user.*;
import com.cheocharm.MapZ.common.jwt.JwtCreateUtils;
import com.cheocharm.MapZ.common.oauth.OauthApi;
import com.cheocharm.MapZ.common.oauth.OauthUrl;
import com.cheocharm.MapZ.common.util.ObjectMapperUtils;
import com.cheocharm.MapZ.common.oauth.GoogleYml;
import com.cheocharm.MapZ.common.util.RandomUtils;
import com.cheocharm.MapZ.common.util.S3Utils;
import com.cheocharm.MapZ.user.domain.dto.*;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;

    private final JwtCreateUtils jwtCreateUtils;
    private final GoogleYml googleYml;
    private final OauthApi oauthApi;
    private final PasswordEncoder passwordEncoder;
    private final S3Utils s3Service;
    private final RandomUtils randomUtils;
    private final JavaMailSender mailSender;
    private final String GMAIL_ADDRESS ="mapz.official@gmail.com";

    @Transactional
    public TokenPairResponseDto signUpGoogle(GoogleSignUpDto userSignUpDto) {

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
    public TokenPairResponseDto loginGoogle(GoogleLoginDto userLoginDto) {
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

    @Transactional
    public TokenPairResponseDto signUpMapZ(MapZSignUpDto mapZSignUpDto, MultipartFile multipartFile) throws IOException {
        //닉네임 중복 확인
        userRepository.findByUsername(mapZSignUpDto.getUsername()).ifPresent(userEntity -> {
            throw new DuplicatedUsernameException();
        });

        TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(mapZSignUpDto.getEmail(), mapZSignUpDto.getUsername());

        if (multipartFile.isEmpty()) {
            userRepository.save(
                    UserEntity.builder()
                            .email(mapZSignUpDto.getEmail())
                            .username(mapZSignUpDto.getUsername())
                            .password(passwordEncoder.encode(mapZSignUpDto.getPassword()))
                            .bio("자기소개를 입력해주세요")
                            .refreshToken(tokenPair.getRefreshToken())
                            .build()
            );

            return tokenPair;
        }

        userRepository.save(
                UserEntity.builder()
                        .email(mapZSignUpDto.getEmail())
                        .username(mapZSignUpDto.getUsername())
                        .password(passwordEncoder.encode(mapZSignUpDto.getPassword()))
                        .userImageUrl(s3Service.uploadUserImage(multipartFile, mapZSignUpDto.getUsername()))
                        .bio("자기소개를 입력해주세요")
                        .refreshToken(tokenPair.getRefreshToken())
                        .build()
        );

        return tokenPair;
    }

    @Transactional
    public TokenPairResponseDto signInMapZ(MapZSignInDto mapZSignInDto) {
        final UserEntity userEntity = userRepository.findByEmail(mapZSignInDto.getEmail())
                .orElseThrow(NotFoundUserException::new);

        //비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(mapZSignInDto.getPassword(), userEntity.getPassword())) {
            throw new WrongPasswordException();
        }

        final TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(userEntity.getEmail(), userEntity.getUsername());
        userEntity.updateRefreshToken(tokenPair.getRefreshToken());
        return tokenPair;

    }

    public String sendEmail(CheckEmailPasswordDto checkEmailPasswordDto) {
        //이메일 중복 확인
        userRepository.findByEmail(checkEmailPasswordDto.getEmail()).ifPresent(userEntity -> {
                    throw new DuplicatedEmailException();
        });

        final String randomNumber = randomUtils.makeRandomNumber();
        final String MAIL_TITLE = String.format("[MapZ] 이메일 인증을 진행해주세요. <%s>", randomNumber);
        final String MAIL_CONTEXT = String.format("안녕하세요. MapZ입니다.\n\n이메일 인증을 위해 아래 숫자를 MapZ에 입력해주세요.\n인증번호: %s\n\n궁금한 사항이 있으시면 mapz.official@gmail.com으로 문의주시길 바랍니다.\n감사합니다:)", randomNumber);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(checkEmailPasswordDto.getEmail());
        message.setFrom(GMAIL_ADDRESS);
        message.setSubject(MAIL_TITLE);
        message.setText(MAIL_CONTEXT);

        mailSender.send(message);

        return randomNumber;

    }

}
