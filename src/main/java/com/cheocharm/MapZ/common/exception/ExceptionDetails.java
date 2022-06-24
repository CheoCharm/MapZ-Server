package com.cheocharm.MapZ.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ExceptionDetails {
    // 공통 에러
    NOT_FOUND_API(HttpStatus.NOT_FOUND, "0001", "존재하지 않는 API입니다. 요청 경로를 확인해주세요."),

    //사용자 관련 에러
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "2001", "유효하지 않은 JWT 입니다. 토큰을 다시 확인해주세요");

    //비즈니스 로직 에러

    private HttpStatus statusCode;
    private String customCode;
    private String message;
}
