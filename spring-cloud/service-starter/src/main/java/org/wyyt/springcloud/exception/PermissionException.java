package org.wyyt.springcloud.exception;


/**
 * the permission exception of db2es
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public final class PermissionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PermissionException(Throwable e) {
        super(e.getMessage(), e);
    }

    public PermissionException(final String errMsg) {
        super(errMsg);
    }
}