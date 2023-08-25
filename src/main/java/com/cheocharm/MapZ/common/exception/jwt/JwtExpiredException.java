package com.cheocharm.MapZ.common.exception.jwt;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class JwtExpiredException extends CustomException {
    public JwtExpiredException(Throwable cause) {
        super(ExceptionDetails.EXPIRED_TOKEN, cause);
    }
}
