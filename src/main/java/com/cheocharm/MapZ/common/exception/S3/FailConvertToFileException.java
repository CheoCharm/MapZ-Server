package com.cheocharm.MapZ.common.exception.S3;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class FailConvertToFileException extends CustomException {
    public FailConvertToFileException() {
        super(ExceptionDetails.FAIL_CONVERT_TO_FILE);
    }
}
