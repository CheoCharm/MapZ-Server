package com.mapz.api.common.exception.diary;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class AlreadyLikedDiaryException extends CustomException {
    public AlreadyLikedDiaryException() {
        super(ExceptionDetails.ALREADY_LIKED_DIARY);
    }
}
