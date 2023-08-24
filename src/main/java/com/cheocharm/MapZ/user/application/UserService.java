package com.cheocharm.MapZ.user.application;

import com.cheocharm.MapZ.agreement.Agreement;
import com.cheocharm.MapZ.agreement.repository.AgreementRepository;
import com.cheocharm.MapZ.common.exception.common.FailJsonProcessException;
import com.cheocharm.MapZ.common.exception.jwt.InvalidJwtException;
import com.cheocharm.MapZ.common.exception.user.DuplicatedEmailException;
import com.cheocharm.MapZ.common.exception.user.DuplicatedUsernameException;
import com.cheocharm.MapZ.common.exception.user.PresentUserException;
import com.cheocharm.MapZ.common.exception.user.WrongPasswordException;
import com.cheocharm.MapZ.common.exception.user.NotFoundUserException;
import com.cheocharm.MapZ.common.image.ImageHandler;
import com.cheocharm.MapZ.common.image.ImageDirectory;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.common.jwt.JwtCreateUtils;
import com.cheocharm.MapZ.common.oauth.OauthApi;
import com.cheocharm.MapZ.common.oauth.OauthUrl;
import com.cheocharm.MapZ.common.util.ObjectMapperUtils;
import com.cheocharm.MapZ.common.oauth.GoogleYml;
import com.cheocharm.MapZ.common.util.RandomUtils;
import com.cheocharm.MapZ.user.domain.User;
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
import com.cheocharm.MapZ.usergroup.domain.UserGroup;
import com.cheocharm.MapZ.usergroup.domain.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.cheocharm.MapZ.common.util.PagingUtils.applyCursorId;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyDescPageConfigBy;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.cheocharm.MapZ.common.util.PagingUtils.USER_SIZE;
import static com.cheocharm.MapZ.user.domain.User.createUserNoPassword;

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
    private final ImageHandler imageHandler;
    private final JavaMailSender mailSender;

    private static final String GMAIL_ADDRESS = "mapz.official@gmail.com";
    private static final String DEFAULT_BIO = "자기소개를 입력해주세요";


    @Transactional
    public TokenPairResponse signUpGoogle(GoogleSignUpRequest request, MultipartFile multipartFile) {
        final GoogleIdTokenResponse idToken = fetchAndValidateIdToken(request.getIdToken());

        userRepository.findByEmailAndUserProvider(idToken.getEmail(), UserProvider.GOOGLE)
                .ifPresent(user -> {
                    throw new PresentUserException();
                });

        final TokenPairResponse tokenPair = jwtCreateUtils.createTokenPair(
                idToken.getEmail(),
                request.getUsername(),
                UserProvider.GOOGLE
        );
        User user = createUserNoPassword(idToken.getEmail(), request.getUsername(), DEFAULT_BIO,
                tokenPair.getRefreshToken(), UserProvider.GOOGLE);

        updateUserImageURL(multipartFile, user);
        saveUserAndAgreement(request.getPushAgreement(), user);
        return tokenPair;
    }

    @Transactional
    public TokenPairResponse signInGoogle(GoogleSignInRequest request) {
        final GoogleIdTokenResponse idToken = fetchAndValidateIdToken(request.getIdToken());

        return userRepository.findByEmailAndUserProvider(idToken.getEmail(), UserProvider.GOOGLE)
                .map(userEntity -> {
                    final TokenPairResponse tokenPair = jwtCreateUtils.createTokenPair(
                            idToken.getEmail(), userEntity.getUsername(), userEntity.getUserProvider()
                    );
                    userEntity.updateRefreshToken(tokenPair.getRefreshToken());
                    return tokenPair;
                })
                .orElseGet(jwtCreateUtils::createNullToken);
    }

    @Transactional
    public TokenPairResponse signUpMapZ(MapZSignUpRequest request, MultipartFile multipartFile) {
        checkDuplicateUserName(request.getUsername());

        TokenPairResponse tokenPair = jwtCreateUtils.createTokenPair(
                request.getEmail(), request.getUsername(), UserProvider.MAPZ
        );
        final User user = createMapZUser(request, multipartFile, tokenPair.getRefreshToken());
        saveUserAndAgreement(request.getPushAgreement(), user);

        return tokenPair;
    }

    private void checkDuplicateUserName(String username) {
        userRepository.findByUsername(username).ifPresent(userEntity -> {
            throw new DuplicatedUsernameException();
        });
    }

    private User createMapZUser(MapZSignUpRequest request, MultipartFile multipartFile, String refreshToken) {
        User user = User.createUser(request.getEmail(), request.getUsername(),
                passwordEncoder.encode(request.getPassword()), DEFAULT_BIO, refreshToken, UserProvider.MAPZ);

        updateUserImageURL(multipartFile, user);
        return user;
    }

    private void saveUserAndAgreement(boolean pushAgreement, User user) {
        userRepository.save(user);
        agreementRepository.save(
                Agreement.of(pushAgreement, user)
        );
    }

    @Transactional
    public TokenPairResponse signInMapZ(MapZSignInRequest request) {
        final User user = validatePresentUserAndReturn(request.getEmail(), UserProvider.MAPZ);

        checkPassword(request.getPassword(), user);

        final TokenPairResponse tokenPair = jwtCreateUtils.createTokenPair(
                user.getEmail(), user.getUsername(), UserProvider.MAPZ
        );
        user.updateRefreshToken(tokenPair.getRefreshToken());
        return tokenPair;
    }

    private void checkPassword(String requestPassword, User user) {
        if (!passwordEncoder.matches(requestPassword, user.getPassword())) {
            throw new WrongPasswordException();
        }
    }

    public String authEmail(String email) {
        checkDuplicateEmail(email);
        return sendEmail(email);
    }

    private void checkDuplicateEmail(String email) {
        userRepository.findByEmailAndUserProvider(email, UserProvider.MAPZ)
                .ifPresent(userEntity -> {
                    throw new DuplicatedEmailException();
                });
    }

    public String findPassword(String email) {
        //가입된 사용자인지 확인
        userRepository.findByEmailAndUserProvider(email, UserProvider.MAPZ)
                .orElseThrow(NotFoundUserException::new);

        return sendEmail(email);

    }

    public String sendEmail(String email) {
        final String randomNumber = RandomUtils.makeRandomNumber();
        final String MAIL_TITLE = String.format("[MapZ] 이메일 인증을 진행해주세요. <%s>", randomNumber);
        final String MAIL_CONTEXT = String.format("안녕하세요. MapZ입니다.\n\n이메일 인증을 위해 아래 숫자를 MapZ에 입력해주세요.\n인증번호: %s\n\n궁금한 사항이 있으시면 mapz.official@gmail.com으로 문의주시길 바랍니다.\n감사합니다:)", randomNumber);

        SimpleMailMessage message = setMassage(email, MAIL_TITLE, MAIL_CONTEXT);

        mailSender.send(message);

        return randomNumber;
    }

    private SimpleMailMessage setMassage(String email, String MAIL_TITLE, String MAIL_CONTEXT) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(GMAIL_ADDRESS);
        message.setSubject(MAIL_TITLE);
        message.setText(MAIL_CONTEXT);
        return message;
    }

    @Transactional
    public void setNewPassword(PasswordChangeRequest request) {
        final User user = validatePresentUserAndReturn(request.getEmail(), UserProvider.MAPZ);

        String password = passwordEncoder.encode(request.getPassword());
        user.updatePassword(password);
    }

    public GetUserListResponse searchUser(Integer page, Long cursorId, String searchName, Long groupId) {
        Slice<User> content = userRepository.fetchByUserAndSearchName(
                UserThreadLocal.get(),
                searchName,
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, USER_SIZE, FIELD_CREATED_AT)
        );

        final List<User> userEntities = content.getContent();
        final List<UserGroup> groupMembers = userGroupRepository.findBySearchNameAndGroupId(searchName, groupId);

        return GetUserListResponse.of(userEntities, groupMembers, content.hasNext());
    }

    public MyPageInfoResponse getMyPageInfo() {
        final User user = UserThreadLocal.get();
        return MyPageInfoResponse.from(user);
    }

    private void updateUserImageURL(MultipartFile multipartFile, User user) {
        if (!multipartFile.isEmpty()) {
            user.updateUserImageUrl(imageHandler.uploadImage(multipartFile, ImageDirectory.USER));
        }
    }

    private User validatePresentUserAndReturn(String email, UserProvider provider) {
        return userRepository.findByEmailAndUserProvider(email, provider)
                .orElseThrow(NotFoundUserException::new);
    }

    private GoogleIdTokenResponse fetchAndValidateIdToken(String idToken) {
        final ResponseEntity<String> response = oauthApi.callGoogle(OauthUrl.GOOGLE, idToken);
        final GoogleIdTokenResponse googleIdToken = getIdToken(response);
        checkAudValue(googleIdToken);
        return googleIdToken;
    }

    private GoogleIdTokenResponse getIdToken(ResponseEntity<String> response) {
        return ObjectMapperUtils.readValue(response.getBody(), GoogleIdTokenResponse.class);
    }

    private void checkAudValue(GoogleIdTokenResponse idToken) {
        if (ObjectUtils.notEqual(idToken.getAud(), googleYml.getClient_id())) {
            throw new InvalidJwtException();
        }
    }

    @Transactional
    public TokenPairResponse refresh(String refreshToken) {
        return jwtCreateUtils.createTokenPair(refreshToken);
    }
}