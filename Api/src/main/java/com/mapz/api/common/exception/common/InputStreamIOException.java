package com.mapz.api.common.exception.common;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class InputStreamIOException extends CustomException {
    public InputStreamIOException(Throwable cause) {
        super(ExceptionDetails.INPUTSTREAM_IO, cause);
    }
}
