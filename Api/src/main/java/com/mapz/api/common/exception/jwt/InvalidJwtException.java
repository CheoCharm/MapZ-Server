package com.mapz.api.common.exception.jwt;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class InvalidJwtException extends CustomException {
    public InvalidJwtException(Throwable cause) {
        super(ExceptionDetails.INVALID_TOKEN, cause);
    }

    public InvalidJwtException() {
        super(ExceptionDetails.INVALID_TOKEN);
    }
}
