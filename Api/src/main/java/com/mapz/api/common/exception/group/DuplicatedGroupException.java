package com.mapz.api.common.exception.group;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class DuplicatedGroupException extends CustomException {
    public DuplicatedGroupException() {
        super(ExceptionDetails.DUPLICATED_GROUP);
    }
}
