package com.mapz.api.common.log.alert;

import javax.servlet.http.HttpServletRequest;

public interface ExceptionAlert {

    void sendExceptionMessage(Exception exception, HttpServletRequest httpServletRequest);

    void sendExceptionMessageWithCause(Exception exception, HttpServletRequest httpServletRequest, Throwable cause);
}
