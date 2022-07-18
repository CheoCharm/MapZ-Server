package com.cheocharm.MapZ.common.exception.user;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class DuplicatedUsernameException extends CustomException {
    public DuplicatedUsernameException() {
        super(ExceptionDetails.DUPLICATED_USERNAME);
    }
}
