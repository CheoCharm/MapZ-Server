package com.cheocharm.MapZ.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private HttpStatus statusCode;
    private String customCode;
    private String message;

    public CustomException(ExceptionDetails exceptionDetails) {
        this.statusCode = exceptionDetails.getStatusCode();
        this.customCode = exceptionDetails.getCustomCode();
        this.message = exceptionDetails.getMessage();
    }
}
