package com.mapz.api.common.exception.S3;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class FailDeleteFileException extends CustomException {
    public FailDeleteFileException() {
        super(ExceptionDetails.FAIL_DELETE_FILE);
    }
}
