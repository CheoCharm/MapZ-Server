package com.cheocharm.MapZ.common.exception.diary;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class NoMatchDiaryIdException extends CustomException {
    public NoMatchDiaryIdException() {
        super(ExceptionDetails.NO_MATCH_DIARY_ID);
    }
}
