package org.wyyt.sharding.db2es.client.metastore;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.internals.KafkaFutureImpl;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.db2es.client.common.CheckpointExt;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.sharding.db2es.client.zookeeper.ZooKeeperWrapper;
import org.wyyt.sharding.db2es.core.entity.domain.Checkpoint;
import org.wyyt.sharding.db2es.core.entity.domain.Common;
import org.wyyt.sharding.db2es.core.util.metastore.MetaStoreUtils;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import static org.wyyt.sharding.db2es.core.util.metastore.MetaStoreUtils.ZOOKEEPER_PATH;

/**
 * Storing the check-point by ZooKeeper
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class ZooKeeperMetaStore implements MetaStore {
    private final Map<String, Map<TopicPartition, Checkpoint>> inMemoryStore;
    private final ZooKeeperWrapper zooKeeperWraper;

    public ZooKeeperMetaStore(final Context context) {
        this.inMemoryStore = new HashMap<>();
        this.zooKeeperWraper = context.getZooKeeperWrapper();
    }

    @Override
    public final Future<CheckpointExt> serializeTo(final String groupName,
                                                   final TopicPartition topicPartition,
                                                   final CheckpointExt checkpointExt) {
        final KafkaFutureImpl result = new KafkaFutureImpl<>();
        Map<TopicPartition, Checkpoint> topicPartitionCheckpointMap = this.inMemoryStore.get(groupName);
        if (null == topicPartitionCheckpointMap) {
            topicPartitionCheckpointMap = new HashMap<>();
        }
        topicPartitionCheckpointMap.put(topicPartition, checkpointExt);
        this.inMemoryStore.put(groupName, topicPartitionCheckpointMap);
        try {
            this.zooKeeperWraper.setData(
                    MetaStoreUtils.getPath(groupName),
                    MetaStoreUtils.toJson(new MetaStoreUtils.StoreElement(groupName, topicPartitionCheckpointMap))
            );
            result.complete(checkpointExt);
        } catch (final Exception exception) {
            result.completeExceptionally(exception);
        }
        return result;
    }

    @Override
    public final CheckpointExt deserializeFrom(final String groupName, final TopicPartition topicPartition) {
        final Map<TopicPartition, Checkpoint> topicPartitionCheckpointMap = this.inMemoryStore.get(groupName);
        if (null != topicPartitionCheckpointMap) {
            final Checkpoint checkpoint = topicPartitionCheckpointMap.get(topicPartition);
            if (null != checkpoint) {
                return new CheckpointExt(checkpoint);
            }
        }
        try {
            final String path = String.format(ZOOKEEPER_PATH, Common.ZK_ROOT_PATH, groupName);
            if (this.zooKeeperWraper.exists(path)) {
                final String json = this.zooKeeperWraper.getData(path);
                if (!ObjectUtils.isEmpty(json)) {
                    final MetaStoreUtils.StoreElement storeElement = MetaStoreUtils.fromString(json);
                    if (!this.inMemoryStore.containsKey(groupName)) {
                        this.inMemoryStore.put(groupName, storeElement.getCheckpointMap());
                    }
                    return new CheckpointExt(storeElement.getCheckpointMap().get(topicPartition));
                }
            }
        } catch (final Exception exception) {
            log.error(String.format("ZooKeeperMetaStore: meet error, cause: [%s]", ExceptionTool.getRootCauseMessage(exception)), exception);
        }
        return null;
    }
}