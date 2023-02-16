package com.cheocharm.MapZ.common.exception.report;

import com.cheocharm.MapZ.common.exception.CustomException;
import com.cheocharm.MapZ.common.exception.ExceptionDetails;

public class AlreadyReportedDiary extends CustomException {
    public AlreadyReportedDiary() {
        super(ExceptionDetails.ALREADY_REPORTED_DIARY);
    }
}
