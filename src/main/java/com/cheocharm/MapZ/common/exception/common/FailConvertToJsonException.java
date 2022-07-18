package com.cheocharm.MapZ.common.exception.common;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class FailConvertToJsonException extends CustomException {
    public FailConvertToJsonException() {
        super(ExceptionDetails.FAIL_CONVERT_TO_JSON);
    }
}