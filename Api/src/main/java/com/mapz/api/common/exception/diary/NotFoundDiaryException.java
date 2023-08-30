package com.mapz.api.common.exception.diary;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class NotFoundDiaryException extends CustomException {
    public NotFoundDiaryException() {
        super(ExceptionDetails.NOT_FOUND_DIARY);
    }
}
