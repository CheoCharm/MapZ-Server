package com.cheocharm.MapZ.common.exception.common;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class FailJsonProcessException extends CustomException {
    public FailJsonProcessException(Throwable cause) {
        super(ExceptionDetails.FAIL_JSON_PROCESS, cause);
    }
}
