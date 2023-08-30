package com.mapz.api.common.exception.S3;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class FailConvertToFileException extends CustomException {
    public FailConvertToFileException() {
        super(ExceptionDetails.FAIL_CONVERT_TO_FILE);
    }
}
