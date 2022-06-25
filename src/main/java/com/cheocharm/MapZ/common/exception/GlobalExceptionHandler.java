package com.cheocharm.MapZ.common.exception;

import com.cheocharm.MapZ.common.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected CommonResponse handleCustomException(CustomException ex) {
        return CommonResponse.fail(ex.getStatusCode(), ex.getCustomCode(), ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    protected CommonResponse handleNoHandlerFoundException(NoHandlerFoundException exception) {
        ExceptionDetails exceptionDetails = ExceptionDetails.NOT_FOUND_API;
        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), exceptionDetails.getMessage());
    }

    @ExceptionHandler(Exception.class)
    protected CommonResponse handleException(Exception ex) {
        ExceptionDetails exceptionDetails = ExceptionDetails.INTERNAL_SERVER_ERROR;
        return CommonResponse.fail(exceptionDetails.getStatusCode(), exceptionDetails.getCustomCode(), exceptionDetails.getMessage());
    }
}
