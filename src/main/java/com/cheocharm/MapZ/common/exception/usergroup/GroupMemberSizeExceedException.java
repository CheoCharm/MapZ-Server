package com.cheocharm.MapZ.common.exception.usergroup;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class GroupMemberSizeExceedException extends CustomException {

    public GroupMemberSizeExceedException() {
        super(ExceptionDetails.GROUP_MEMBER_SIZE_EXCEED);
    }
}
