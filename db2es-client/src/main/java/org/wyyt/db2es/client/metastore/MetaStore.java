package org.wyyt.db2es.client.metastore;


import org.apache.kafka.common.TopicPartition;
import org.wyyt.db2es.client.common.CheckpointExt;

import java.util.concurrent.Future;

/**
 * the interface of how to store check-point
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public interface MetaStore {
    Future<CheckpointExt> serializeTo(final String groupName,
                                      final TopicPartition topicPartition,
                                      final CheckpointExt checkpoint);

    CheckpointExt deserializeFrom(final String groupName,
                                  final TopicPartition topicPartition);
}