package com.cheocharm.MapZ.common.exception.user;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class NoPermissionUserException extends CustomException {
    public NoPermissionUserException() {
        super(ExceptionDetails.NO_PERMISSION_USER);
    }
}
