package com.cheocharm.MapZ.common.exception.user;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class DuplicatedEmailException extends CustomException {
    public DuplicatedEmailException() {
        super(ExceptionDetails.DUPLICATED_EMAIL);
    }
}
