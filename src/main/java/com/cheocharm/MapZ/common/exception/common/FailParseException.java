package com.cheocharm.MapZ.common.exception.common;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class FailParseException extends CustomException {
    public FailParseException(Throwable cause) {
        super(ExceptionDetails.FAIL_PARSE, cause);
    }
}
