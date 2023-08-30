package com.mapz.api.common.exception.user;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class DuplicatedUsernameException extends CustomException {
    public DuplicatedUsernameException() {
        super(ExceptionDetails.DUPLICATED_USERNAME);
    }
}
