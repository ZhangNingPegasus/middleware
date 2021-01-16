package org.wyyt.sharding.db2es.core.util.flatmsg;

import org.wyyt.sharding.db2es.core.entity.domain.FlatMsg;

/**
 * the interface of operation
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public interface Operation<T extends FlatMsg> {
    void insert(final T message) throws Exception;

    void delete(final T message) throws Exception;

    void update(final T message) throws Exception;

    void exception(final T message, final Exception exception) throws Exception;
}