package com.mapz.api.common.exception;

import com.mapz.api.common.CommonResponse;
import com.mapz.api.common.exception.jwt.InvalidJwtException;
import com.mapz.api.common.exception.jwt.JwtExpiredException;
import com.mapz.api.common.exception.report.AlreadyReportedDiary;
import com.mapz.api.common.exception.user.DuplicatedEmailException;
import com.mapz.api.common.exception.user.ExitGroupChiefException;
import com.mapz.api.common.exception.user.NoPermissionUserException;
import com.mapz.api.common.exception.usergroup.GroupMemberSizeExceedException;
import com.mapz.api.common.exception.usergroup.SelfKickException;
import com.mapz.api.common.log.alert.ExceptionAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ExceptionAlert exceptionAlert;

    public GlobalExceptionHandler(ExceptionAlert exceptionAlert) {
        this.exceptionAlert = exceptionAlert;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            ExitGroupChiefException.class,
            InvalidJwtException.class,
            GroupMemberSizeExceedException.class,
            SelfKickException.class,
            DuplicatedEmailException.class,
            AlreadyReportedDiary.class
    })
    protected CommonResponse<?> handleBadRequest(CustomException exception, HttpServletRequest httpServletRequest) {
        exceptionAlert.sendExceptionMessage(exception, httpServletRequest);
        return CommonResponse.fail(exception.getStatusCode(), exception.getCustomCode(), exception.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({
            NoPermissionUserException.class,
    })
    protected CommonResponse<?> handleForbidden(CustomException exception) {
        return CommonResponse.fail(exception.getStatusCode(), exception.getCustomCode(), exception.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    protected CommonResponse<?> handleCustomException(CustomException exception, HttpServletRequest httpServletRequest) {
        if (Objects.nonNull(exception.getCause())) {
            exceptionAlert.sendExceptionMessageWithCause(exception, httpServletRequest, exception.getCause());
        } else {
            exceptionAlert.sendExceptionMessage(exception, httpServletRequest);
        }
        return CommonResponse.fail(exception.getStatusCode(), exception.getCustomCode(), exception.getMessage());
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
    protected CommonResponse<?> handleConstraintViolationException(ConstraintViolationException exception) {
        ExceptionDetails exceptionDetails = ExceptionDetails.CONSTRAINT_VIOLATION;
        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    protected CommonResponse<?> handleException(Exception exception, HttpServletRequest httpServletRequest) {
        ExceptionDetails exceptionDetails = ExceptionDetails.INTERNAL_SERVER_ERROR;
        logger.error("error message => {}", exception.getCause().toString());
        exceptionAlert.sendExceptionMessageWithCause(exception, httpServletRequest, exception.getCause());
        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), exceptionDetails.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(JwtExpiredException.class)
    protected CommonResponse<?> handleJwtExpiredException(JwtExpiredException exception, HttpServletRequest httpServletRequest) {
        exceptionAlert.sendExceptionMessage(exception, httpServletRequest);
        return CommonResponse.fail(exception.getStatusCode(), exception.getCustomCode(), exception.getMessage());
    }
}
