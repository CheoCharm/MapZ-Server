package com.cheocharm.MapZ.common.exception.user;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class PresentUserException extends CustomException {
    public PresentUserException() {
        super(ExceptionDetails.PRESENT_USER);
    }
}
