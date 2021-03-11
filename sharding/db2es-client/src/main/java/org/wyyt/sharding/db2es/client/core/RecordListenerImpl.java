package org.wyyt.sharding.db2es.client.core;

import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.sharding.db2es.client.common.RecordListener;
import org.wyyt.sharding.db2es.client.entity.FlatMessage;

import java.util.List;

/**
 * the default implementation of RecordListener
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class RecordListenerImpl implements RecordListener {
    private final Context context;

    public RecordListenerImpl(final Context context) {
        this.context = context;
    }

    @Override
    public int consume(final List<FlatMessage> flatMessageList) throws Exception {
        if (null == flatMessageList || flatMessageList.isEmpty()) {
            return 0;
        }
        int result = this.context.getElasticSearchWrapper().populate(flatMessageList);
        final FlatMessage lastFlatMessage = flatMessageList.get(flatMessageList.size() - 1);
        lastFlatMessage.commit(this.context);
        return result;
    }
}