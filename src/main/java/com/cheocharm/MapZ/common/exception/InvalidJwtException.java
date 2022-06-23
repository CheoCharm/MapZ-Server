package com.cheocharm.MapZ.common.exception;

public class InvalidJwtException extends CustomException{
    public InvalidJwtException() {
        super(ExceptionDetails.INVALID_TOKEN);
    }
}
