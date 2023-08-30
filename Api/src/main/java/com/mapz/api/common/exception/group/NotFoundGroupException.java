package com.mapz.api.common.exception.group;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class NotFoundGroupException extends CustomException {
    public NotFoundGroupException() {
        super(ExceptionDetails.NOT_FOUND_GROUP);
    }
}
