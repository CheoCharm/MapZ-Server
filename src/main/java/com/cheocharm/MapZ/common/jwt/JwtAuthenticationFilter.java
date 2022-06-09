package com.cheocharm.MapZ.common.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationUtils jwtAuthenticationUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = jwtAuthenticationUtils.resolveToken(request);

        try {
            if (token.isPresent() && jwtAuthenticationUtils.isValid(token.get())) {
                Authentication auth = jwtAuthenticationUtils.getAuthentication(token.get());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }
        filterChain.doFilter(request, response);
    }
}
