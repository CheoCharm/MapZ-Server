package com.cheocharm.MapZ.common.exception.group;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class DuplicatedGroupException extends CustomException {
    public DuplicatedGroupException() {
        super(ExceptionDetails.DUPLICATED_GROUP);
    }
}
