package com.cheocharm.MapZ.common.exception.user;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class NotFoundUserException extends CustomException {
    public NotFoundUserException() {
        super(ExceptionDetails.NOT_FOUND_USER);
    }
}
