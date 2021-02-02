package org.wyyt.sharding.db2es.client.metastore;

import org.apache.kafka.common.TopicPartition;
import org.wyyt.sharding.db2es.client.common.CheckpointExt;

import java.util.concurrent.Future;

/**
 * the interface of how to store check-point
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public interface MetaStore {
    Future<CheckpointExt> serializeTo(final String groupName,
                                      final TopicPartition topicPartition,
                                      final CheckpointExt checkpoint);

    CheckpointExt deserializeFrom(final String groupName,
                                  final TopicPartition topicPartition);
}