package com.mapz.api.common.exception.common;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class FailParseException extends CustomException {
    public FailParseException(Throwable cause) {
        super(ExceptionDetails.FAIL_PARSE, cause);
    }
}
