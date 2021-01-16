package org.wyyt.admin.ui.exception;


import org.wyyt.tool.exception.ExceptionTool;

/**
 * the business exception of db2es
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public final class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(Throwable e) {
        super(ExceptionTool.getRootCauseMessage(e), e);
    }

    public BusinessException(final String errMsg) {
        super(errMsg);
    }
}