package org.wyyt.springcloud.entity.exception;


import org.wyyt.tool.exception.ExceptionTool;

/**
 * the business exception of db2es
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public final class GatewayException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GatewayException(Throwable e) {
        super(ExceptionTool.getRootCauseMessage(e), e);
    }

    public GatewayException(final String errMsg) {
        super(errMsg);
    }
}