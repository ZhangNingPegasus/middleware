package org.wyyt.db2es.client.metastore;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.internals.KafkaFutureImpl;
import org.wyyt.db2es.client.common.CheckpointExt;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Storing the check-point by Kafka
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class KafkaMetaStore implements MetaStore {
    private final KafkaConsumer kafkaConsumer;

    public KafkaMetaStore(final KafkaConsumer kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    @Override
    public final Future<CheckpointExt> serializeTo(final String groupName,
                                                   final TopicPartition topicPartition,
                                                   final CheckpointExt checkpoint) {
        final KafkaFutureImpl<CheckpointExt> result = new KafkaFutureImpl<>();
        if (null == this.kafkaConsumer) {
            String errorMsg = String.format("KafkaMetaData: KafkaConsumer not be initialized, failed to serialize metastore for group [%s] and topic partition [%s]", groupName, topicPartition);
            log.error(errorMsg);
            throw new KafkaException(errorMsg);
        }

        final OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(checkpoint.getOffset(), String.valueOf(checkpoint.getTimestamp()));
        // commitAsync异步提交仅仅只是把提交偏移量的请求发送到Kafka的队列中，具体的执行需要通过调用KafkaConsumer.poll()方法
        // 因此调用这个方法的时候，一定需要调用poll方法
        this.kafkaConsumer.commitAsync(Collections.singletonMap(topicPartition, offsetAndMetadata), (map, exception) -> {
            if (null == exception) {
                log.debug(String.format("KafkaMetaStore: Commit offset success for group [%s] topic partition [%s] %s",
                        groupName,
                        topicPartition.toString(),
                        checkpoint));
                result.complete(checkpoint);
            } else {
                log.error(String.format("KafkaMetaStore: Commit offset for group [%s] topic partition[%s] %s failed cause %s",
                        groupName,
                        topicPartition.toString(),
                        checkpoint.toString(),
                        exception));
                result.completeExceptionally(exception);
            }
        });
        return result;
    }

    @Override
    public final CheckpointExt deserializeFrom(final String groupName,
                                               final TopicPartition topicPartition) {
        if (null == this.kafkaConsumer) {
            String errorMsg = String.format("KafkaMetaData: KafkaConsumer not be initialized, failed to deserialize metastore for group [%s] and topic partition [%s]", groupName, topicPartition);
            log.error(errorMsg);
            throw new KafkaException(errorMsg);
        }

        final Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap = this.kafkaConsumer.committed(Collections.singleton(topicPartition));
        if (null == offsetAndMetadataMap || offsetAndMetadataMap.isEmpty()) {
            return null;
        } else {
            final OffsetAndMetadata offsetAndMetadata = offsetAndMetadataMap.get(topicPartition);
            if (null == offsetAndMetadata) {
                return null;
            }
            return new CheckpointExt(topicPartition, offsetAndMetadata.offset(), Long.parseLong(offsetAndMetadata.metadata()));
        }
    }
}