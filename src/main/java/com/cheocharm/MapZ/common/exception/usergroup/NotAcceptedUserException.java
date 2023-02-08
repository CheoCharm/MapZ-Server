package com.cheocharm.MapZ.common.exception.usergroup;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class NotAcceptedUserException extends CustomException {

    public NotAcceptedUserException() {
        super(ExceptionDetails.NOT_ACCEPTED_USER);
    }
}
