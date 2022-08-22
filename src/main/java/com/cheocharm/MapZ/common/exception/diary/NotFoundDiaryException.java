package com.cheocharm.MapZ.common.exception.diary;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class NotFoundDiaryException extends CustomException {
    public NotFoundDiaryException() {
        super(ExceptionDetails.NOT_FOUND_DIARY);
    }
}
