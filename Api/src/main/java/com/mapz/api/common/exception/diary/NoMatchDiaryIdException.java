package com.mapz.api.common.exception.diary;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class NoMatchDiaryIdException extends CustomException {
    public NoMatchDiaryIdException() {
        super(ExceptionDetails.NO_MATCH_DIARY_ID);
    }
}
