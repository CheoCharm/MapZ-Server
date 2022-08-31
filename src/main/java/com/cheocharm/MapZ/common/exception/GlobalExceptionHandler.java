package com.cheocharm.MapZ.common.exception;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.common.exception.jwt.JwtExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected CommonResponse<?> handleCustomException(CustomException ex) {
        return CommonResponse.fail(ex.getStatusCode(), ex.getCustomCode(), ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    protected CommonResponse<?> handleNoHandlerFoundException() {
        ExceptionDetails exceptionDetails = ExceptionDetails.NOT_FOUND_API;
        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), exceptionDetails.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected CommonResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ExceptionDetails exceptionDetails = ExceptionDetails.INVALID_USER_INFO;
        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected CommonResponse<?> handleConstraintViolationException(ConstraintViolationException ex) {
        ExceptionDetails exceptionDetails = ExceptionDetails.CONSTRAINT_VIOLATION;
        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    protected CommonResponse<?> handleException() {
        ExceptionDetails exceptionDetails = ExceptionDetails.INTERNAL_SERVER_ERROR;
        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), exceptionDetails.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(JwtExpiredException.class)
    protected CommonResponse<?> handleJwtExpiredException() {
        ExceptionDetails exceptionDetails = ExceptionDetails.EXPIRED_TOKEN;
        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), exceptionDetails.getMessage());
    }
}
