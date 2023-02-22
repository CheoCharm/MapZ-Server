package com.cheocharm.MapZ.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus statusCode;
    private final String customCode;
    private final String message;
    private Throwable cause;

    public CustomException(ExceptionDetails exceptionDetails) {
        this.statusCode = exceptionDetails.getStatusCode();
        this.customCode = exceptionDetails.getCustomCode();
        this.message = exceptionDetails.getMessage();
    }

    public CustomException(ExceptionDetails exceptionDetails, Throwable cause) {
        this.statusCode = exceptionDetails.getStatusCode();
        this.customCode = exceptionDetails.getCustomCode();
        this.message = exceptionDetails.getMessage();
        this.cause = cause;
    }
}
