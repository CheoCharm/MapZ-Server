package com.mapz.api.common.jwt;

import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.enums.UserProvider;
import com.mapz.domain.domains.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiredArgsConstructor
@Component
public class JwtCommonUtils {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private static final String USER_EMAIL = "email";

    private final UserRepository userRepository;

    @PostConstruct
    protected void init() {
        SECRET_KEY = Base64.getEncoder().encodeToString(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public User findUserByToken(String token) {
        return userRepository.findByEmailAndUserProvider(findEmailByToken(token), findUserProviderByToken(token))
                .orElseThrow(() -> new RuntimeException("이메일 유저 정보 없음"));
    }

    public String findEmailByToken(String token) {
        return (String) Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(USER_EMAIL);
    }

    public UserProvider findUserProviderByToken(String token) {
        String userProvider = (String) Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userProvider");

        return UserProvider.valueOf(userProvider);

    }

    public String getKey() {
        return SECRET_KEY;
    }
}
