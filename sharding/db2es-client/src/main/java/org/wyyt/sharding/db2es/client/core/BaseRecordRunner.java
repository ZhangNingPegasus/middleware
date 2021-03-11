package org.wyyt.sharding.db2es.client.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.util.Assert;
import org.wyyt.sharding.db2es.client.common.CheckpointExt;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.sharding.db2es.client.kafka.KafkaConsumerWrapper;
import org.wyyt.sharding.db2es.client.kafka.KafkaConsumerWrapperFactory;
import org.wyyt.sharding.db2es.client.metastore.KafkaMetaStore;
import org.wyyt.sharding.db2es.client.metastore.MetaStoreCenter;
import org.wyyt.sharding.db2es.client.metastore.ZooKeeperMetaStore;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * the base class used for polling the records from kafka server
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public abstract class BaseRecordRunner extends BaseRunner {
    protected static final int TRY_TIME = 150;
    protected static final long TRY_BACK_TIME_MS = 10L * 1000L;
    protected final MetaStoreCenter metaStoreCenter;
    protected final String groupName;
    private static final String KAFKA_STORE_NAME = "kafka_checkpoint_store";
    private static final String ZOOKEEPER_STORE_NAME = "zookeeper_checkpoint_store";

    private final KafkaConsumerWrapperFactory kafkaConsumerWrapFactory;
    protected final AtomicBoolean useCheckpointConfig;
    @Getter
    public final TopicPartition topicPartition;
    @Getter
    private CheckpointExt initialCheckpoint;

    public BaseRecordRunner(final Context context,
                            final String topicName,
                            final int partition,
                            final String groupName,
                            @Nullable final CheckpointExt initialCheckpoint,
                            final KafkaConsumerWrapperFactory kafkaConsumerWrapFactory) {
        super(context);
        this.topicPartition = new TopicPartition(topicName, partition);
        this.groupName = groupName;
        this.kafkaConsumerWrapFactory = kafkaConsumerWrapFactory;
        this.metaStoreCenter = new MetaStoreCenter();
        this.initialCheckpoint = initialCheckpoint;
        this.useCheckpointConfig = new AtomicBoolean(null != initialCheckpoint);
        this.metaStoreCenter.register(ZOOKEEPER_STORE_NAME, new ZooKeeperMetaStore(context));
    }

    protected KafkaConsumerWrapper getKafkaConsumerWrap() {
        final KafkaConsumerWrapper kafkaConsumerWrap = this.kafkaConsumerWrapFactory.getConsumerWrap(this.groupName, this.context);
        this.metaStoreCenter.register(KAFKA_STORE_NAME, new KafkaMetaStore(kafkaConsumerWrap.getKafkaConsumer()));
        CheckpointExt checkpointExt;

        if (this.useCheckpointConfig.compareAndSet(true, false)) {
            Assert.notNull(this.initialCheckpoint, "Checkpoint不允许为空");
            checkpointExt = this.initialCheckpoint;
        } else {
            checkpointExt = getCheckpoint();
            this.initialCheckpoint = checkpointExt;
        }

        log.info(String.format("RecordRunner: topic[%s] will try to use checkpoint[timestamp=%s, offset=%s] to start",
                this.topicPartition,
                checkpointExt.getTimestamp(),
                checkpointExt.getOffset()));

        kafkaConsumerWrap.assign(this.topicPartition, checkpointExt);
        return kafkaConsumerWrap;
    }

    protected CheckpointExt getCheckpoint() {
        CheckpointExt result = this.metaStoreCenter.seek(ZOOKEEPER_STORE_NAME, this.groupName, this.topicPartition);
        if (null == result) {
            result = this.metaStoreCenter.seek(KAFKA_STORE_NAME, this.groupName, this.topicPartition);
        }
        if (null == result) {
            result = CheckpointExt.INITIALIZED_CHECKPOINT;
        }
        return result;
    }

    @Override
    public final void close() {
        this.terminated = true;
    }
}