package com.mapz.api.common.log.response;

import com.mapz.api.common.CommonResponse;
import com.mapz.api.common.exception.common.FailConvertToJsonException;
import com.mapz.api.common.util.ObjectMapperUtils;
import lombok.Builder;
import lombok.Getter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Getter
@Builder
public class ResponseLogSchema {
    private String api;
    private String httpMethod;
    private int statusCode;
    private String message;

    public static ResponseLogSchema createLogSchema(HttpServletRequest request, CommonResponse response) {
        return ResponseLogSchema.builder()
                .api(request.getRequestURI())
                .httpMethod(request.getMethod())
                .statusCode(response.getStatusCode())
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
