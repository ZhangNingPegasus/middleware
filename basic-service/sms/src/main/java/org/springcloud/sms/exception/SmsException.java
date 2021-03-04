package org.springcloud.sms.exception;

import org.wyyt.tool.exception.ExceptionTool;

import java.io.Serializable;

/**
 * the business exception of sms
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public final class SmsException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;

    public SmsException(Throwable e) {
        super(ExceptionTool.getRootCauseMessage(e), e);
    }

    public SmsException(final String errMsg) {
        super(errMsg);
    }
}
