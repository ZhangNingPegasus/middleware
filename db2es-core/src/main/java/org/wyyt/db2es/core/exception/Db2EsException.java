package org.wyyt.db2es.core.exception;


import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

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

    public Db2EsException(final String errMsg) {
        super(errMsg);
    }

    @Override
    public final String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}