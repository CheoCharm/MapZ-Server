package com.cheocharm.MapZ.user.domain;

import com.cheocharm.MapZ.agreement.AgreementEntity;
import com.cheocharm.MapZ.agreement.repository.AgreementRepository;
import com.cheocharm.MapZ.common.exception.jwt.InvalidJwtException;
import com.cheocharm.MapZ.common.exception.user.*;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
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
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.cheocharm.MapZ.common.util.PagingUtils.*;

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

        Optional<UserEntity> findUser = userRepository.findByEmailAndUserProvider(idToken.getEmail(), UserProvider.GOOGLE);

        if (findUser.isPresent()) {
            final UserEntity userEntity = findUser.get();
            final TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(idToken.getEmail(), userSignUpDto.getUsername(), UserProvider.GOOGLE);
            userEntity.updateRefreshToken(tokenPair.getRefreshToken());
            return tokenPair;
        }

        final TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(idToken.getEmail(), userSignUpDto.getUsername(), UserProvider.GOOGLE);

        final UserEntity user = userRepository.save(
                UserEntity.builder()
                        .email(idToken.getEmail())
                        .username(userSignUpDto.getUsername())
                        .bio("자기소개를 입력해주세요")
                        .refreshToken(tokenPair.getRefreshToken())
                        .userProvider(UserProvider.GOOGLE)
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

        Optional<UserEntity> findUser = userRepository.findByEmailAndUserProvider(idToken.getEmail(), UserProvider.GOOGLE);

        if (findUser.isPresent()) {
            final UserEntity userEntity = findUser.get();
            final TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(idToken.getEmail(), userEntity.getUsername(), userEntity.getUserProvider());
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
    public TokenPairResponseDto signUpMapZ(MapZSignUpDto mapZSignUpDto, MultipartFile multipartFile) {
        //닉네임 중복 확인
        userRepository.findByUsername(mapZSignUpDto.getUsername()).ifPresent(userEntity -> {
            throw new DuplicatedUsernameException();
        });

        TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(mapZSignUpDto.getEmail(), mapZSignUpDto.getUsername(), UserProvider.MAPZ);
        UserEntity userEntity = UserEntity.builder()
                .email(mapZSignUpDto.getEmail())
                .username(mapZSignUpDto.getUsername())
                .password(passwordEncoder.encode(mapZSignUpDto.getPassword()))
                .bio("자기소개를 입력해주세요")
                .refreshToken(tokenPair.getRefreshToken())
                .userProvider(UserProvider.MAPZ)
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
        final UserEntity userEntity = userRepository.findByEmailAndUserProvider(mapZSignInDto.getEmail(), UserProvider.MAPZ)
                .orElseThrow(NotFoundUserException::new);

        //비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(mapZSignInDto.getPassword(), userEntity.getPassword())) {
            throw new WrongPasswordException();
        }

        final TokenPairResponseDto tokenPair = jwtCreateUtils.createTokenPair(userEntity.getEmail(), userEntity.getUsername(), UserProvider.MAPZ);
        userEntity.updateRefreshToken(tokenPair.getRefreshToken());
        return tokenPair;

    }

    public String authEmail(String email) {
        //이메일 중복 확인
        userRepository.findByEmailAndUserProvider(email, UserProvider.MAPZ)
                .ifPresent(userEntity -> {
                    throw new DuplicatedEmailException();
                });

        return sendEmail(email);
    }

    public String findPassword(String email) {
        //가입된 사용자인지 확인
        userRepository.findByEmailAndUserProvider(email, UserProvider.MAPZ)
                .orElseThrow(NotFoundUserException::new);

        return sendEmail(email);

    }

    public String sendEmail(String email) {
        final String randomNumber = randomUtils.makeRandomNumber();
        final String MAIL_TITLE = String.format("[MapZ] 이메일 인증을 진행해주세요. <%s>", randomNumber);
        final String MAIL_CONTEXT = String.format("안녕하세요. MapZ입니다.\n\n이메일 인증을 위해 아래 숫자를 MapZ에 입력해주세요.\n인증번호: %s\n\n궁금한 사항이 있으시면 mapz.official@gmail.com으로 문의주시길 바랍니다.\n감사합니다:)", randomNumber);

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
        UserEntity userEntity = userRepository.findByEmailAndUserProvider(getNewPasswordDto.getEmail(), UserProvider.MAPZ)
                .orElseThrow(NotFoundUserException::new);

        String password = passwordEncoder.encode(getNewPasswordDto.getPassword());
        userEntity.updatePassword(password);
    }

    public GetUserListDto searchUser(Integer page, String searchName) {
        Slice<UserEntity> content = userRepository.fetchByUserEntityAndSearchName(
                UserThreadLocal.get(),
                searchName,
                applyAscPageConfigBy(page, USER_SIZE, FIELD_USERNAME)
        );

        final List<UserEntity> userEntityList = content.getContent();

        List<GetUserListDto.UserList> userList = userEntityList.stream()
                .map(userEntity ->
                        GetUserListDto.UserList.builder()
                                .username(userEntity.getUsername())
                                .userImageUrl(userEntity.getUserImageUrl())
                                .build()
                )
                .collect(Collectors.toList());

        return GetUserListDto.builder()
                .hasNext(content.hasNext())
                .userList(userList)
                .build();
    }
}