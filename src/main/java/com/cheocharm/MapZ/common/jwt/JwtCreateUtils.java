package com.cheocharm.MapZ.common.jwt;

import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.user.domain.dto.TokenPairResponseDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JwtCreateUtils {

    private final String USER_EMAIL = "email";
    private final long EXPIRE = 1000 * 60 * 30;
    private final long REFRESH_EXPIRE = EXPIRE * 2 * 24 * 14;

    private final JwtCommonUtils jwtCommonUtils;

    public String createAccessToken(String email, String username) {
        Date issueDate = new Date();
        Date expireDate = new Date();
        expireDate.setTime(issueDate.getTime() + EXPIRE);
        return Jwts.builder()
                .setHeaderParams(createHeader())
                .setClaims(createClaims(email, username))
                .setIssuedAt(issueDate)
                .setExpiration(expireDate)
                .setSubject("accessToken")
                .signWith(Keys.hmacShaKeyFor(jwtCommonUtils.getKey().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public String createAccessToken(String refreshToken) {
        final UserEntity userEntity = jwtCommonUtils.findUserByToken(refreshToken);
        if(!userEntity.getRefreshToken().equals(refreshToken)){
            throw new RuntimeException("토큰 정보 불일치");
        }
        return createAccessToken(userEntity.getEmail(), userEntity.getUsername());
    }

    public String createRefreshToken(String email, String username) {
        Date issueDate = new Date();
        Date expireDate = new Date();
        expireDate.setTime(issueDate.getTime() + REFRESH_EXPIRE);
        return Jwts.builder()
                .setHeaderParams(createHeader())
                .setClaims(createClaims(email, username))
                .setIssuedAt(issueDate)
                .setExpiration(expireDate)
                .setSubject("refreshToken")
                .signWith(Keys.hmacShaKeyFor(jwtCommonUtils.getKey().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public TokenPairResponseDto createTokenPair(String email, String username) {
        return TokenPairResponseDto.builder()
                .accessToken(createAccessToken(email, username))
                .refreshToken(createRefreshToken(email, username))
                .build();
    }

    private Map<String, Object> createHeader() {
        HashMap<String, Object> headerMap = new HashMap<>();
        headerMap.put("typ", "JWT");
        return headerMap;
    }

    private Map<String, Object> createClaims(String email, String username) {
        HashMap<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(USER_EMAIL, email);
        claimsMap.put("username", username);
        return claimsMap;
    }


    public TokenPairResponseDto createNullToken() {
        return TokenPairResponseDto.builder()
                .build();
    }
}
