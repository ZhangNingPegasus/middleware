package org.wyyt.sharding.db2es.client.kafka;

import org.wyyt.sharding.db2es.client.common.Context;

/**
 * the factory of creation for KafkaConsumerWrapper
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public final class KafkaConsumerWrapperFactory {
    public final KafkaConsumerWrapper getConsumerWrap(final String groupName,
                                                      final Context context) {
        return new KafkaConsumerWrapper(groupName, context);
    }
}