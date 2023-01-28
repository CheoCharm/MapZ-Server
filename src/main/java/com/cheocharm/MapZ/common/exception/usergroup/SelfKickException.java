package com.cheocharm.MapZ.common.exception.usergroup;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class SelfKickException extends CustomException {

    public SelfKickException() {
        super(ExceptionDetails.SELF_KICK);
    }
}
