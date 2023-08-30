package com.mapz.api.common.exception.user;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class PresentUserException extends CustomException {
    public PresentUserException() {
        super(ExceptionDetails.PRESENT_USER);
    }
}
