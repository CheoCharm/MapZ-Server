package com.cheocharm.MapZ.common.interceptor;

import com.cheocharm.MapZ.common.jwt.JwtCommonUtils;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtCommonUtils jwtCommonUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws RuntimeException {
        String token = request.getHeader("token");

        if (token == null) {
            throw new RuntimeException("token value null");
        }

        UserEntity user = jwtCommonUtils.findUserByToken(token);
        UserThreadLocal.set(user);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        UserThreadLocal.remove();
    }
}
