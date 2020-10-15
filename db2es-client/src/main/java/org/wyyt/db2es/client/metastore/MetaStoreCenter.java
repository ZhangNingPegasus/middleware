package org.wyyt.db2es.client.metastore;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.wyyt.db2es.client.common.CheckpointExt;

import java.util.HashMap;
import java.util.Map;

/**
 * The check-point management center
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class MetaStoreCenter {
    private final Map<String, MetaStore> registerMetaStore;

    public MetaStoreCenter() {
        this.registerMetaStore = new HashMap<>();
    }

    public final void register(final String storeName,
                               final MetaStore metaStore) {
        this.registerMetaStore.put(storeName, metaStore);
    }

    public final void store(final String groupName,
                            final TopicPartition topicPartition,
                            final CheckpointExt checkpoint) {
        for (final Map.Entry<String, MetaStore> pair : this.registerMetaStore.entrySet()) {
            pair.getValue().serializeTo(groupName, topicPartition, checkpoint);
        }
    }

    public final CheckpointExt seek(final String storeName,
                                    final String groupName,
                                    final TopicPartition topicPartition) {
        final MetaStore metaStore = registerMetaStore.get(storeName);
        if (null != metaStore) {
            return metaStore.deserializeFrom(groupName, topicPartition);
        } else {
            return null;
        }
    }
}