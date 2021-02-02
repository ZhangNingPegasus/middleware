package org.wyyt.springcloud.gateway.entity.exception;

import org.wyyt.tool.exception.ExceptionTool;

/**
 * the business exception of db2es
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
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