package com.mapz.api.common.exception.user;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class WrongPasswordException extends CustomException {
    public WrongPasswordException() {
        super(ExceptionDetails.WRONG_PASSWORD);
    }
}
