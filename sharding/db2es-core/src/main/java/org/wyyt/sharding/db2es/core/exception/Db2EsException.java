package org.wyyt.sharding.db2es.core.exception;


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
public final class Db2EsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public Db2EsException(Throwable e) {
        super(ExceptionTool.getRootCauseMessage(e), e);
    }

    public Db2EsException(final String errMsg) {
        super(errMsg);
    }
}