package com.cheocharm.MapZ.common.log.exception;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.common.exception.common.FailConvertToJsonException;
import com.cheocharm.MapZ.common.util.ObjectMapperUtils;
import lombok.Builder;
import lombok.Getter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Getter
@Builder
public class ExceptionLogSchema {
    private String api;
    private String httpMethod;
    private int statusCode;
    private String customCode;
    private String message;

    public static ExceptionLogSchema createLogSchema(HttpServletRequest request, CommonResponse response) {
        return ExceptionLogSchema.builder()
                .api(request.getRequestURI())
                .httpMethod(request.getMethod())
                .statusCode(response.getStatusCode())
                .customCode(response.getCustomCode())
                .message(response.getMessage())
                .build();
    }

    @Override
    public String toString() {
        String json = "";
        try {
            json = ObjectMapperUtils.getObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            throw new FailConvertToJsonException();
        }

        return json;
    }
}
