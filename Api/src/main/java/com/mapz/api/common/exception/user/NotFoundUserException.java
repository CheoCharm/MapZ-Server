package com.mapz.api.common.exception.user;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class NotFoundUserException extends CustomException {
    public NotFoundUserException() {
        super(ExceptionDetails.NOT_FOUND_USER);
    }
}
