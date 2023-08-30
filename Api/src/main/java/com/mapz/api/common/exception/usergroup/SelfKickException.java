package com.mapz.api.common.exception.usergroup;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class SelfKickException extends CustomException {

    public SelfKickException() {
        super(ExceptionDetails.SELF_KICK);
    }
}
