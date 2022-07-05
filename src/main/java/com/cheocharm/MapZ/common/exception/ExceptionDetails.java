package com.cheocharm.MapZ.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ExceptionDetails {
    // 공통 에러
    NOT_FOUND_API(HttpStatus.NOT_FOUND, "0001", "존재하지 않는 API입니다. 요청 경로를 확인해주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "0002", "서버 에러입니다. 서버측에 문의주세요."),
    FAIL_CONVERT_TO_JSON(HttpStatus.INTERNAL_SERVER_ERROR, "0003", "JSON으로 변환하는 도중 오류가 발생하였습니다."),

    // S3 에러
    FAIL_CONVERT_TO_FILE(HttpStatus.BAD_REQUEST, "1001", "이미지를 파일로 변경하는데 실패하였습니다."),
    FAIL_DELETE_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "1002", "로컬에 저장된 이미지를 삭제하는데 실패하였습니다."),

    // 사용자 관련 에러
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "2001", "유효하지 않은 JWT 입니다. 토큰을 다시 확인해주세요");


    // 비즈니스 로직 에러

    private HttpStatus statusCode;
    private String customCode;
    private String message;
}
