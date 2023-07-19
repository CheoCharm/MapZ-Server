package com.cheocharm.MapZ.user.application;

import com.cheocharm.MapZ.agreement.AgreementEntity;
import com.cheocharm.MapZ.agreement.repository.AgreementRepository;
import com.cheocharm.MapZ.common.exception.common.FailJsonProcessException;
import com.cheocharm.MapZ.common.exception.jwt.InvalidJwtException;
import com.cheocharm.MapZ.common.exception.user.DuplicatedEmailException;
import com.cheocharm.MapZ.common.exception.user.DuplicatedUsernameException;
import com.cheocharm.MapZ.common.exception.user.WrongPasswordException;
import com.cheocharm.MapZ.common.exception.user.NotFoundUserException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.common.jwt.JwtCreateUtils;
import com.cheocharm.MapZ.common.oauth.OauthApi;
import com.cheocharm.MapZ.common.oauth.OauthUrl;
import com.cheocharm.MapZ.common.util.ObjectMapperUtils;
import com.cheocharm.MapZ.common.oauth.GoogleYml;
import com.cheocharm.MapZ.common.util.RandomUtils;
import com.cheocharm.MapZ.common.util.S3Utils;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.user.domain.UserProvider;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import com.cheocharm.MapZ.user.presentation.dto.request.GoogleSignInRequest;
import com.cheocharm.MapZ.user.presentation.dto.request.GoogleSignUpRequest;
import com.cheocharm.MapZ.user.presentation.dto.request.MapZSignInRequest;
import com.cheocharm.MapZ.user.presentation.dto.request.MapZSignUpRequest;
import com.cheocharm.MapZ.user.presentation.dto.request.PasswordChangeRequest;
import com.cheocharm.MapZ.user.presentation.dto.response.GetUserListResponse;
import com.cheocharm.MapZ.user.presentation.dto.response.GoogleIdTokenResponse;
import com.cheocharm.MapZ.user.presentation.dto.response.MyPageInfoResponse;
import com.cheocharm.MapZ.user.presentation.dto.response.TokenPairResponse;
import com.cheocharm.MapZ.usergroup.domain.UserGroupEntity;
import com.cheocharm.MapZ.usergroup.domain.repository.UserGroupRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
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

import static com.cheocharm.MapZ.common.util.PagingUtils.applyCursorId;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyDescPageConfigBy;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.cheocharm.MapZ.common.util.PagingUtils.USER_SIZE;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AgreementRepository agreementRepository;
    private final UserGroupRepository userGroupRepository;

    private final JwtCreateUtils jwtCreateUtils;
    private final GoogleYml googleYml;
    private final OauthApi oauthApi;
    private final PasswordEncoder passwordEncoder;
    private final S3Utils s3Service;
    private final RandomUtils randomUtils;
    private final JavaMailSender mailSender;
    private final String GMAIL_ADDRESS ="mapz.official@gmail.com";

    @Transactional
    public TokenPairResponse signUpGoogle(GoogleSignUpRequest userSignUpDto, MultipartFile multipartFile) {

        final ResponseEntity<String> response = oauthApi.callGoogle(OauthUrl.GOOGLE, userSignUpDto.getIdToken());

        if (ObjectUtils.notEqual(response.getStatusCode(), HttpStatus.OK)) {
            throw new InvalidJwtException();
        }

        final GoogleIdTokenResponse idToken = checkAudAndGetTokenDto(response);

        Optional<UserEntity> findUser = userRepository.findByEmailAndUserProvider(idToken.getEmail(), UserProvider.GOOGLE);

        if (findUser.isPresent()) {
            final UserEntity userEntity = findUser.get();
            final TokenPairResponse tokenPair = jwtCreateUtils.createTokenPair(idToken.getEmail(),
                    userSignUpDto.getUsername(), UserProvider.GOOGLE);
            userEntity.updateRefreshToken(tokenPair.getRefreshToken());
            return tokenPair;
        }

        final TokenPairResponse tokenPair = jwtCreateUtils.createTokenPair(idToken.getEmail(),
                userSignUpDto.getUsername(), UserProvider.GOOGLE);

        final UserEntity user = userRepository.save(
                UserEntity.createUserNoPassword(idToken.getEmail(), userSignUpDto.getUsername(),
                        tokenPair.getRefreshToken(), UserProvider.GOOGLE)
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
    public TokenPairResponse loginGoogle(GoogleSignInRequest userLoginDto) {
        ResponseEntity<String> response = oauthApi.callGoogle(OauthUrl.GOOGLE, userLoginDto.getIdToken());

        if (ObjectUtils.notEqual(response.getStatusCode(), HttpStatus.OK)) {
            throw new InvalidJwtException();
        }
        GoogleIdTokenResponse idToken = checkAudAndGetTokenDto(response);

        Optional<UserEntity> findUser = userRepository.findByEmailAndUserProvider(idToken.getEmail(), UserProvider.GOOGLE);

        if (findUser.isPresent()) {
            final UserEntity userEntity = findUser.get();
            final TokenPairResponse tokenPair = jwtCreateUtils.createTokenPair(idToken.getEmail(), userEntity.getUsername(), userEntity.getUserProvider());
            userEntity.updateRefreshToken(tokenPair.getRefreshToken());
            return tokenPair;
        }

        return jwtCreateUtils.createNullToken();
    }

    @Transactional
    public TokenPairResponse signUpMapZ(MapZSignUpRequest request, MultipartFile multipartFile) {
        //닉네임 중복 확인
        userRepository.findByUsername(request.getUsername()).ifPresent(userEntity -> {
            throw new DuplicatedUsernameException();
        });

        TokenPairResponse tokenPair = jwtCreateUtils.createTokenPair(request.getEmail(), request.getUsername(), UserProvider.MAPZ);
        UserEntity userEntity = UserEntity.createUser(request.getEmail(), request.getUsername(),
                passwordEncoder.encode(request.getPassword()), tokenPair.getRefreshToken(), UserProvider.MAPZ);

        if (!multipartFile.isEmpty()) {
            userEntity.updateUserImageUrl(s3Service.uploadUserImage(multipartFile, request.getUsername()));
        }

        UserEntity user = userRepository.save(userEntity);
        agreementRepository.save(
                AgreementEntity.builder()
                    .userEntity(user)
                    .pushAgreement(request.getPushAgreement())
                    .build()
        );

        return tokenPair;
    }

    @Transactional
    public TokenPairResponse signInMapZ(MapZSignInRequest mapZSignInRequest) {
        final UserEntity userEntity = userRepository.findByEmailAndUserProvider(mapZSignInRequest.getEmail(), UserProvider.MAPZ)
                .orElseThrow(NotFoundUserException::new);

        //비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(mapZSignInRequest.getPassword(), userEntity.getPassword())) {
            throw new WrongPasswordException();
        }

        final TokenPairResponse tokenPair = jwtCreateUtils.createTokenPair(userEntity.getEmail(), userEntity.getUsername(), UserProvider.MAPZ);
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
    public void setNewPassword(PasswordChangeRequest passWordChangeRequest) {
        UserEntity userEntity = userRepository.findByEmailAndUserProvider(passWordChangeRequest.getEmail(), UserProvider.MAPZ)
                .orElseThrow(NotFoundUserException::new);

        String password = passwordEncoder.encode(passWordChangeRequest.getPassword());
        userEntity.updatePassword(password);
    }

    public GetUserListResponse searchUser(Integer page, Long cursorId, String searchName, Long groupId) {
        Slice<UserEntity> content = userRepository.fetchByUserEntityAndSearchName(
                UserThreadLocal.get(),
                searchName,
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, USER_SIZE, FIELD_CREATED_AT)
        );

        final List<UserEntity> userEntities = content.getContent();
        final List<UserGroupEntity> groupMembers = userGroupRepository.findBySearchNameAndGroupId(searchName, groupId);

        return GetUserListResponse.of(userEntities, groupMembers, content.hasNext());
    }

    public MyPageInfoResponse getMyPageInfo() {
        final UserEntity userEntity = UserThreadLocal.get();

        return MyPageInfoResponse.from(userEntity);
    }

    private GoogleIdTokenResponse checkAudAndGetTokenDto(ResponseEntity<String> response) {
        try {
            GoogleIdTokenResponse idToken = ObjectMapperUtils.getObjectMapper().readValue(response.getBody(), GoogleIdTokenResponse.class);

            if (ObjectUtils.notEqual(idToken.getAud(), googleYml.getClient_id())) {
                throw new InvalidJwtException();
            }
            return idToken;
        } catch (JsonProcessingException e) {
            throw new FailJsonProcessException(e);
        }
    }

}