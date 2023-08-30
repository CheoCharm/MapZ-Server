package com.mapz.api.common.exception.user;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class ExitGroupChiefException extends CustomException {
    public ExitGroupChiefException() {
        super(ExceptionDetails.EXIT_GROUP_CHIEF);
    }
}
