package com.mapz.api.common.exception.user;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class NoPermissionUserException extends CustomException {
    public NoPermissionUserException() {
        super(ExceptionDetails.NO_PERMISSION_USER);
    }
}
