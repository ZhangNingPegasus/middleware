package org.wyyt.sharding.db2es.client.common;

import org.wyyt.sharding.db2es.client.entity.FlatMessage;

import java.util.List;

/**
 * the listener which invoked when kafka records arrivals.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public interface RecordListener {
    int consume(final List<FlatMessage> flatMessageList) throws Exception;
}