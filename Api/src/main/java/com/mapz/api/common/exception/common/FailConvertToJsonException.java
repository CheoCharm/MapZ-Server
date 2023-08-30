package com.mapz.api.common.exception.common;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class FailConvertToJsonException extends CustomException {
    public FailConvertToJsonException() {
        super(ExceptionDetails.FAIL_CONVERT_TO_JSON);
    }
}