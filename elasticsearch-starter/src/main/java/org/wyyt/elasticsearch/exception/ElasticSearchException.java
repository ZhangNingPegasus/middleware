package org.wyyt.elasticsearch.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * The exception of ElasticSearch
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public final class ElasticSearchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ElasticSearchException(Throwable e) {
        super(ExceptionUtils.getRootCauseMessage(e), e);
    }

    public ElasticSearchException(final String errMsg) {
        super(errMsg);
    }
}