package com.mapz.api.common.exception.user;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class DuplicatedEmailException extends CustomException {
    public DuplicatedEmailException() {
        super(ExceptionDetails.DUPLICATED_EMAIL);
    }
}
