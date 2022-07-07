package com.cheocharm.MapZ.common.exception.user;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class WrongPasswordException extends CustomException {
    public WrongPasswordException() {
        super(ExceptionDetails.WRONG_PASSWORD);
    }
}
