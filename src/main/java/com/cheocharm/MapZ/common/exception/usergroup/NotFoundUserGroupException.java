package com.cheocharm.MapZ.common.exception.usergroup;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class NotFoundUserGroupException extends CustomException {

    public NotFoundUserGroupException() {
        super(ExceptionDetails.NOT_FOUND_USERGROUP);
    }
}
