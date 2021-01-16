package org.wyyt.springcloud.exception;


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
        super(e.getMessage(), e);
    }

    public BusinessException(final String errMsg) {
        super(errMsg);
    }
}