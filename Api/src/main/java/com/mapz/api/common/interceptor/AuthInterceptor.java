package com.mapz.api.common.interceptor;

import com.mapz.api.common.jwt.JwtCommonUtils;
import com.mapz.domain.domains.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtCommonUtils jwtCommonUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws RuntimeException {
        String token = request.getHeader("accessToken");

        if (token == null) {
            throw new RuntimeException("token value null");
        }

        User user = jwtCommonUtils.findUserByToken(token);
        UserThreadLocal.set(user);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        UserThreadLocal.remove();
    }
}
