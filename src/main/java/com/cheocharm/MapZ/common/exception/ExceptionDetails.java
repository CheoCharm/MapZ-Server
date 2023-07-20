package com.cheocharm.MapZ.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ExceptionDetails {
    // 공통 에러
    NOT_FOUND_API(HttpStatus.NOT_FOUND, "0001", "존재하지 않는 API입니다. 요청 경로를 확인해주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "0002", "서버 에러입니다. 서버측에 문의주세요. 메시지는: "),
    FAIL_CONVERT_TO_JSON(HttpStatus.INTERNAL_SERVER_ERROR, "0003", "JSON으로 변환하는 도중 오류가 발생하였습니다."),
    FAIL_JSON_PROCESS(HttpStatus.INTERNAL_SERVER_ERROR, "0004", "JSON에서 클래스로 변환하는 도중 오류가 발생했습니다."),
    FAIL_PARSE(HttpStatus.INTERNAL_SERVER_ERROR, "0005", "파싱에 실패했습니다."),
    INPUTSTREAM_IO(HttpStatus.INTERNAL_SERVER_ERROR, "0006", "입출력 예외입니다"),

    // S3 에러
    FAIL_CONVERT_TO_FILE(HttpStatus.BAD_REQUEST, "1001", "이미지를 파일로 변경하는데 실패하였습니다."),
    FAIL_DELETE_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "1002", "로컬에 저장된 이미지를 삭제하는데 실패하였습니다."),

    // 사용자 관련 에러
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "2001", "유효하지 않은 JWT 입니다. 토큰을 다시 확인해주세요."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "2002", "만료된 JWT 입니다."),
    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "2003", "중복된 이메일입니다."),
    DUPLICATED_USERNAME(HttpStatus.BAD_REQUEST, "2004", "중복된 닉네임입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "2005", "가입된 사용자가 아닙니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "2006", "비밀번호가 일치하지 않습니다."),
    INVALID_USER_INFO(HttpStatus.BAD_REQUEST, "2007", "유효성 검사 통과에 실패하였습니다."),
    NO_PERMISSION_USER(HttpStatus.BAD_REQUEST, "2008", "허가되지 않은 유저입니다."),
    EXIT_GROUP_CHIEF(HttpStatus.BAD_REQUEST, "2009", "그룹장은 그룹을 나갈 수 없습니다."),
    CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "2010", "올바른 형식의 이메일 주소여야 합니다"),

    //그룹 관련 에러
    NOT_FOUND_GROUP(HttpStatus.NOT_FOUND, "3001", "해당되는 그룹이 없습니다."),
    DUPLICATED_GROUP(HttpStatus.BAD_REQUEST, "3002", "중복된 그룹명입니다."),

    //사용자 그룹(userGroup) 예외
    NOT_FOUND_USERGROUP(HttpStatus.NOT_FOUND, "4001", "해당되는 유저그룹 테이블 데이터가 없습니다."),
    SELF_KICK(HttpStatus.BAD_REQUEST, "4002", "내보내려는 유저와 요청한 유저가 동일합니다."),
    NOT_ACCEPTED_USER(HttpStatus.BAD_REQUEST, "4003", "수락된 유저가 아닙니다."),
    GROUP_MEMBER_SIZE_EXCEED(HttpStatus.BAD_REQUEST, "4004", "그룹 유저 제한 수를 초과했습니다"),

    //다이어리 예외
    NOT_FOUND_DIARY(HttpStatus.NOT_FOUND, "5001", "게시글을 찾을 수 없습니다."),
    ALREADY_LIKED_DIARY(HttpStatus.BAD_REQUEST, "5002", "이미 좋아요한 게시글입니다."),
    NO_MATCH_DIARY_ID(HttpStatus.NOT_FOUND, "5003", "매칭되는 다이어리 아이디가 없습니다."),

    //신고 예외
    ALREADY_REPORTED_DIARY(HttpStatus.BAD_REQUEST, "6001", "이미 신고한 게시글입니다.");

    // 비즈니스 로직 에러

    private HttpStatus statusCode;
    private String customCode;
    private String message;
}
