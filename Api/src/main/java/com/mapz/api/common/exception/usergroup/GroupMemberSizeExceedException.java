package com.mapz.api.common.exception.usergroup;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class GroupMemberSizeExceedException extends CustomException {

    public GroupMemberSizeExceedException() {
        super(ExceptionDetails.GROUP_MEMBER_SIZE_EXCEED);
    }
}
