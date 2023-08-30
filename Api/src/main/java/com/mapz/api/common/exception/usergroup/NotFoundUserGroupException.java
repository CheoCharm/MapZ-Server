package com.mapz.api.common.exception.usergroup;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class NotFoundUserGroupException extends CustomException {

    public NotFoundUserGroupException() {
        super(ExceptionDetails.NOT_FOUND_USERGROUP);
    }
}
