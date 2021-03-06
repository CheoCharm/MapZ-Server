package com.cheocharm.MapZ.user.domain;

import com.cheocharm.MapZ.agreement.AgreementEntity;
import com.cheocharm.MapZ.agreement.repository.AgreementRepository;
import com.cheocharm.MapZ.common.exception.jwt.InvalidJwtException;
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

import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AgreementRepository agreementRepository;

    private final JwtCreateUtils jwtCreateUtils;
    private final GoogleYml googleYml;
    private final OauthApi oauthApi;
    private final PasswordEncoder passwordEncoder;
    private final S3Utils s3Service;
    private final RandomUtils randomUtils;
    private final JavaMailSender mailSender;
    private final String GMAIL_ADDRESS ="mapz.official@gmail.com";

    @Transactional
    public TokenPairResponseDto signUpGoogle(GoogleSignUpDto userSignUpDto, MultipartFile multipartFile) {

        final ResponseEntity<String> response = oauthApi.callGoogle(OauthUrl.GOOGLE, userSignUpDto.getIdToken());

        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            throw new InvalidJwtException();
        }

        final GoogleIdTokenDto idToken = checkAudAndGetTokenDto(response);

        Optional<UserEntity> findUser = userRepository.findByEmail(idToken.getEmail());

        if (findUser.isPresent()) {
            final UserEntity userEntity = findUser.get();
            final TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(idToken.getEmail(), userSignUpDto.getUsername());
            userEntity.updateRefreshToken(tokenPair.getRefreshToken());
            return tokenPair;
        }

        final TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(idToken.getEmail(), userSignUpDto.getUsername());

        final UserEntity user = userRepository.save(
                UserEntity.builder()
                        .email(idToken.getEmail())
                        .username(userSignUpDto.getUsername())
                        .bio("??????????????? ??????????????????")
                        .refreshToken(tokenPair.getRefreshToken())
                        .build()
        );
        if (!multipartFile.isEmpty()) {
            user.updateUserImageUrl(s3Service.uploadUserImage(multipartFile, user.getUsername()));
        }
        agreementRepository.save(
                AgreementEntity.builder()
                    .userEntity(user)
                    .pushAgreement(userSignUpDto.getPushAgreement())
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
            throw new RuntimeException("readvalue ??????");
        }
    }

    @Transactional
    public TokenPairResponseDto signUpMapZ(MapZSignUpDto mapZSignUpDto, MultipartFile multipartFile) {
        //????????? ?????? ??????
        userRepository.findByUsername(mapZSignUpDto.getUsername()).ifPresent(userEntity -> {
            throw new DuplicatedUsernameException();
        });

        TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(mapZSignUpDto.getEmail(), mapZSignUpDto.getUsername());
        UserEntity userEntity = UserEntity.builder()
                .email(mapZSignUpDto.getEmail())
                .username(mapZSignUpDto.getUsername())
                .password(passwordEncoder.encode(mapZSignUpDto.getPassword()))
                .bio("??????????????? ??????????????????")
                .refreshToken(tokenPair.getRefreshToken())
                .build();

        if (!multipartFile.isEmpty()) {
            userEntity.updateUserImageUrl(s3Service.uploadUserImage(multipartFile, mapZSignUpDto.getUsername()));
        }

        UserEntity user = userRepository.save(userEntity);
        agreementRepository.save(
                AgreementEntity.builder()
                    .userEntity(user)
                    .pushAgreement(mapZSignUpDto.getPushAgreement())
                    .build()
        );

        return tokenPair;
    }

    @Transactional
    public TokenPairResponseDto signInMapZ(MapZSignInDto mapZSignInDto) {
        final UserEntity userEntity = userRepository.findByEmail(mapZSignInDto.getEmail())
                .orElseThrow(NotFoundUserException::new);

        //???????????? ?????? ?????? ??????
        if (!passwordEncoder.matches(mapZSignInDto.getPassword(), userEntity.getPassword())) {
            throw new WrongPasswordException();
        }

        final TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(userEntity.getEmail(), userEntity.getUsername());
        userEntity.updateRefreshToken(tokenPair.getRefreshToken());
        return tokenPair;

    }

    public String authEmail(CheckEmailDto checkEmailPasswordDto) {
        //????????? ?????? ??????
        userRepository.findByEmail(checkEmailPasswordDto.getEmail()).ifPresent(userEntity -> {
            throw new DuplicatedEmailException();
        });

        return sendEmail(checkEmailPasswordDto.getEmail());

    }

    public String findPassword(FindPasswordDto findPasswordDto) {
        //????????? ??????????????? ??????
        userRepository.findByEmail(findPasswordDto.getEmail()).orElseThrow(NotFoundUserException::new);

        //?????? ???????????? ?????? ????????????

        return sendEmail(findPasswordDto.getEmail());

    }

    public String sendEmail(String email) {
        final String randomNumber = randomUtils.makeRandomNumber();
        final String MAIL_TITLE = String.format("[MapZ] ????????? ????????? ??????????????????. <%s>", randomNumber);
        final String MAIL_CONTEXT = String.format("???????????????. MapZ?????????.\n\n????????? ????????? ?????? ?????? ????????? MapZ??? ??????????????????.\n????????????: %s\n\n????????? ????????? ???????????? mapz.official@gmail.com?????? ??????????????? ????????????.\n???????????????:)", randomNumber);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(GMAIL_ADDRESS);
        message.setSubject(MAIL_TITLE);
        message.setText(MAIL_CONTEXT);

        mailSender.send(message);

        return randomNumber;
    }

    @Transactional
    public void setNewPassword(GetNewPasswordDto getNewPasswordDto) {
        UserEntity userEntity = userRepository.findByEmail(getNewPasswordDto.getEmail()).orElseThrow(NotFoundUserException::new);
        String password = passwordEncoder.encode(getNewPasswordDto.getPassword());
        userEntity.updatePassword(password);
    }

}