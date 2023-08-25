package com.cheocharm.MapZ.common.exception;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.common.exception.jwt.InvalidJwtException;
import com.cheocharm.MapZ.common.exception.jwt.JwtExpiredException;
import com.cheocharm.MapZ.common.exception.report.AlreadyReportedDiary;
import com.cheocharm.MapZ.common.exception.user.DuplicatedEmailException;
import com.cheocharm.MapZ.common.exception.user.ExitGroupChiefException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.exception.usergroup.GroupMemberSizeExceedException;
import com.cheocharm.MapZ.common.exception.usergroup.SelfKickException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            ExitGroupChiefException.class,
            InvalidJwtException.class,
            GroupMemberSizeExceedException.class,
            SelfKickException.class,
            DuplicatedEmailException.class,
            AlreadyReportedDiary.class
    })
    protected CommonResponse<?> handleBadRequest(CustomException e) {
        return CommonResponse.fail(e.getStatusCode(), e.getCustomCode(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({
            NoPermissionUserException.class,
    })
    protected CommonResponse<?> handleForbidden(CustomException e) {
        return CommonResponse.fail(e.getStatusCode(), e.getCustomCode(), e.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    protected CommonResponse<?> handleCustomException(CustomException ex) {
        if (Objects.nonNull(ex.getCause())) {
            logger.error("error message => {}", ex.getCause().toString());
        }
        return CommonResponse.fail(ex.getStatusCode(), ex.getCustomCode(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    protected CommonResponse<?> handleNoHandlerFoundException() {
        ExceptionDetails exceptionDetails = ExceptionDetails.NOT_FOUND_API;
        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), exceptionDetails.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected CommonResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ExceptionDetails exceptionDetails = ExceptionDetails.INVALID_USER_INFO;
        final ObjectError objectError = ex.getBindingResult().getAllErrors().get(0);

        StringBuilder sb = new StringBuilder();
        sb.append(objectError.getObjectName()).append("에서 발생한 문제입니다. ").append(objectError.getDefaultMessage());
        logger.error("error message => {}", sb);

        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), exceptionDetails.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    protected CommonResponse<?> handleConstraintViolationException(ConstraintViolationException ex) {
        ExceptionDetails exceptionDetails = ExceptionDetails.CONSTRAINT_VIOLATION;
        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    protected CommonResponse<?> handleException(Exception ex) {
        ExceptionDetails exceptionDetails = ExceptionDetails.INTERNAL_SERVER_ERROR;
        logger.error("error message => {}", ex.getCause().toString());

        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), exceptionDetails.getMessage().concat(ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(JwtExpiredException.class)
    protected CommonResponse<?> handleJwtExpiredException(JwtExpiredException ex) {
        ExceptionDetails exceptionDetails = ExceptionDetails.EXPIRED_TOKEN;
        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), exceptionDetails.getMessage());
    }
}
