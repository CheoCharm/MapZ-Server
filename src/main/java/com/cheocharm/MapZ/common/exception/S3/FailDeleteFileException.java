package com.cheocharm.MapZ.common.exception.S3;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class FailDeleteFileException extends CustomException {
    public FailDeleteFileException() {
        super(ExceptionDetails.FAIL_DELETE_FILE);
    }
}
