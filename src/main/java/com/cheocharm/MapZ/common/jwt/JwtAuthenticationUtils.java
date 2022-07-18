package com.cheocharm.MapZ.common.jwt;

import com.cheocharm.MapZ.common.exception.jwt.InvalidJwtException;
import com.cheocharm.MapZ.common.exception.jwt.JwtExpiredException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationUtils {

    private final JwtCommonUtils jwtCommonUtils;

    public Optional<String> resolveToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("token"));
    }

    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtCommonUtils.getKey().getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (SignatureException | MalformedJwtException | IllegalArgumentException | UnsupportedJwtException e) {
            throw new InvalidJwtException();
        } catch (ExpiredJwtException e) {
            throw new JwtExpiredException();
        }
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = new User(jwtCommonUtils.findEmailByToken(token), "", getAuthorities());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private Set<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> set = new HashSet<>();
        set.add(new SimpleGrantedAuthority("ROLE_USER"));

        return set;
    }
}
