package com.cheocharm.MapZ.common.exception.diary;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class AlreadyLikedDiaryException extends CustomException {
    public AlreadyLikedDiaryException() {
        super(ExceptionDetails.ALREADY_LIKED_DIARY);
    }
}
