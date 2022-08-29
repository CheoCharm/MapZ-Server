package com.cheocharm.MapZ.common;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class CommonResponse<T> {

    private int statusCode;
    private String customCode;
    private T data;
    private String message;

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .statusCode(HttpStatus.OK.value())
                .customCode("0000")
                .message(HttpStatus.OK.toString())
                .data(data)
                .build();
    }

    public static <T> CommonResponse<T> success() {
        return CommonResponse.<T>builder()
                .statusCode(HttpStatus.OK.value())
                .customCode("0000")
                .message(HttpStatus.OK.toString())
                .data(null)
                .build();
    }

    public static <T extends HttpStatus> CommonResponse<T> fail(T statusCode, String customCode, String message) {
        return CommonResponse.<T>builder()
                .statusCode(statusCode.value())
                .customCode(customCode)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> CommonResponse<T> expiredToken() {
        return CommonResponse.<T>builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message(HttpStatus.UNAUTHORIZED.toString())
                .data(null)
                .build();
    }
}
