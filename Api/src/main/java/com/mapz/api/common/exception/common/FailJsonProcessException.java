package com.mapz.api.common.exception.common;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class FailJsonProcessException extends CustomException {
    public FailJsonProcessException(Throwable cause) {
        super(ExceptionDetails.FAIL_JSON_PROCESS, cause);
    }
}
