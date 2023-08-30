package com.mapz.api.common.exception;

public class InternalServerException extends CustomException {
    public InternalServerException() {
        super(ExceptionDetails.INTERNAL_SERVER_ERROR);
    }
}
