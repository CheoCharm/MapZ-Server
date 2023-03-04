package com.cheocharm.MapZ.common.jwt;

import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.user.domain.UserProvider;
import com.cheocharm.MapZ.user.presentation.dto.response.TokenPairResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JwtCreateUtils {

    private final String USER_EMAIL = "email";
    private final long EXPIRE = 1000 * 60 * 60;
    private final long REFRESH_EXPIRE = EXPIRE * 2 * 24 * 14;

    private final JwtCommonUtils jwtCommonUtils;

    public String createAccessToken(String email, String username, UserProvider userProvider) {
        Date issueDate = new Date();
        Date expireDate = new Date();
        expireDate.setTime(issueDate.getTime() + EXPIRE);
        return Jwts.builder()
                .setHeaderParams(createHeader())
                .setClaims(createClaims(email, username, userProvider))
                .setIssuedAt(issueDate)
                .setExpiration(expireDate)
                .setSubject("accessToken")
                .signWith(Keys.hmacShaKeyFor(jwtCommonUtils.getKey().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Transactional
    public TokenPairResponse createAccessToken(String refreshToken) {
        final UserEntity userEntity = jwtCommonUtils.findUserByToken(refreshToken);
        if(!userEntity.getRefreshToken().equals(refreshToken)){
            throw new RuntimeException("토큰 정보 불일치");
        }
        final TokenPairResponse tokenPair = createTokenPair(userEntity.getEmail(), userEntity.getUsername(), userEntity.getUserProvider());
        userEntity.updateRefreshToken(tokenPair.getRefreshToken());
        return tokenPair;
    }

    public String createRefreshToken(String email, String username, UserProvider userProvider) {
        Date issueDate = new Date();
        Date expireDate = new Date();
        expireDate.setTime(issueDate.getTime() + REFRESH_EXPIRE);
        return Jwts.builder()
                .setHeaderParams(createHeader())
                .setClaims(createClaims(email, username, userProvider))
                .setIssuedAt(issueDate)
                .setExpiration(expireDate)
                .setSubject("refreshToken")
                .signWith(Keys.hmacShaKeyFor(jwtCommonUtils.getKey().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public TokenPairResponse createTokenPair(String email, String username, UserProvider userProvider) {
        return TokenPairResponse.builder()
                .accessToken(createAccessToken(email, username, userProvider))
                .refreshToken(createRefreshToken(email, username, userProvider))
                .build();
    }

    private Map<String, Object> createHeader() {
        HashMap<String, Object> headerMap = new HashMap<>();
        headerMap.put("typ", "JWT");
        return headerMap;
    }

    private Map<String, Object> createClaims(String email, String username, UserProvider userProvider) {
        HashMap<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(USER_EMAIL, email);
        claimsMap.put("username", username);
        claimsMap.put("userProvider", userProvider);
        return claimsMap;
    }


    public TokenPairResponse createNullToken() {
        return TokenPairResponse.builder()
                .build();
    }
}
