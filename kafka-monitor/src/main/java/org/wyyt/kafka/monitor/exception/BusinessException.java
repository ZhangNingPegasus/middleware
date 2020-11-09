package org.wyyt.kafka.monitor.exception;

import org.wyyt.tool.exception.ExceptionTool;

/**
 * the customized exception for business
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(Throwable e) {
        super(ExceptionTool.getRootCauseMessage(e), e);
    }

    public BusinessException(final String errMsg) {
        super(errMsg);
    }
}