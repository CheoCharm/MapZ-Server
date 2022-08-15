package com.cheocharm.MapZ.common.exception.user;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class ExitGroupChiefException extends CustomException {
    public ExitGroupChiefException() {
        super(ExceptionDetails.EXIT_GROUP_CHIEF);
    }
}
