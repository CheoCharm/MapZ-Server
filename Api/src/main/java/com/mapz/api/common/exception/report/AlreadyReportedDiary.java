package com.mapz.api.common.exception.report;

import com.mapz.api.common.exception.CustomException;
import com.mapz.api.common.exception.ExceptionDetails;

public class AlreadyReportedDiary extends CustomException {
    public AlreadyReportedDiary() {
        super(ExceptionDetails.ALREADY_REPORTED_DIARY);
    }
}
