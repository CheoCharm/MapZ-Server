package com.cheocharm.MapZ.common.config;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.common.util.ObjectMapperUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationEntryPointCustom implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        String errorResponseJson = ObjectMapperUtils.getObjectMapper().writeValueAsString(
                CommonResponse.fail(HttpStatus.FORBIDDEN, "-1", "FORBIDDEN")
        );

        response.setStatus(403);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(errorResponseJson);
    }
}
