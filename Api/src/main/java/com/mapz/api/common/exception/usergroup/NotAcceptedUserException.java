package com.mapz.api.common.exception.usergroup;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class NotAcceptedUserException extends CustomException {

    public NotAcceptedUserException() {
        super(ExceptionDetails.NOT_ACCEPTED_USER);
    }
}
