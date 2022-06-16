package com.cheocharm.MapZ.common.exception;

import com.cheocharm.MapZ.common.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected CommonResponse handleCustomException(CustomException ex) {
        return CommonResponse.fail(ex.getStatusCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    protected CommonResponse handleException(CustomException ex) {
        return CommonResponse.fail(ex.getStatusCode(), ex.getMessage());
    }
}
