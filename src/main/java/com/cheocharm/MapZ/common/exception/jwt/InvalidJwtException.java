package com.cheocharm.MapZ.common.exception.jwt;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class InvalidJwtException extends CustomException {
    public InvalidJwtException() {
        super(ExceptionDetails.INVALID_TOKEN);
    }
}
