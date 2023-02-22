package com.cheocharm.MapZ.common.exception.common;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class InputStreamIOException extends CustomException {
    public InputStreamIOException(Throwable cause) {
        super(ExceptionDetails.INPUTSTREAM_IO, cause);
    }
}
