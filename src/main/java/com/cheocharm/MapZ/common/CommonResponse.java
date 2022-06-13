package com.cheocharm.MapZ.common;

import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public class CommonResponse<T> {

    private int code;
    private T data;
    private String message;

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.toString())
                .data(data)
                .build();
    }

    public static <T> CommonResponse<T> success() {
        return CommonResponse.<T>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.toString())
                .data(null)
                .build();
    }

    public static <T extends HttpStatus> CommonResponse<T> fail(T status, String message) {
        return CommonResponse.<T>builder()
                .code(status.value())
                .message(message)
                .data(null)
                .build();
    }

    public static <T> CommonResponse<T> expiredToken() {
        return CommonResponse.<T>builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message(HttpStatus.UNAUTHORIZED.toString())
                .data(null)
                .build();
    }
}
