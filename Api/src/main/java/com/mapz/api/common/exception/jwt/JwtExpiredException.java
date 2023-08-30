package com.mapz.api.common.exception.jwt;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class JwtExpiredException extends CustomException {
    public JwtExpiredException(Throwable cause) {
        super(ExceptionDetails.EXPIRED_TOKEN, cause);
    }
}
