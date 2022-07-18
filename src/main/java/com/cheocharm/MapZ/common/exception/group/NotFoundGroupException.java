package com.cheocharm.MapZ.common.exception.group;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class NotFoundGroupException extends CustomException {
    public NotFoundGroupException() {
        super(ExceptionDetails.NOT_FOUND_GROUP);
    }
}
