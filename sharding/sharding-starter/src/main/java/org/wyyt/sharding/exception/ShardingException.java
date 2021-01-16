package org.wyyt.sharding.exception;

import org.wyyt.tool.exception.ExceptionTool;

/**
 * The exception of ShardingSphere
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public final class ShardingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ShardingException(Throwable e) {
        super(ExceptionTool.getRootCauseMessage(e), e);
    }

    public ShardingException(final String errMsg) {
        super(errMsg);
    }
}