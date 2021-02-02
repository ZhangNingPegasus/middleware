package org.wyyt.sharding.db2es.client.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.wyyt.sharding.db2es.core.entity.domain.TopicOffset;
import org.wyyt.sharding.db2es.core.util.kafka.KafkaUtils;
import org.wyyt.sharding.db2es.client.common.Context;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * the wrapper class of Kafka Admin Client
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class KafkaAdminClientWrapper implements Closeable {

    private final KafkaAdminClient kafkaAdminClient;

    public KafkaAdminClientWrapper(final Context context) {
        final Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, context.getKafkaBootstrapServers());
        this.kafkaAdminClient = (KafkaAdminClient) AdminClient.create(properties);
    }

    public final Map<TopicPartition, TopicOffset> listOffset(final List<TopicPartition> topicPartitionList) throws Exception {
        final Map<TopicPartition, TopicOffset> result = new HashMap<>();
        final Map<TopicPartition, OffsetSpec> topicPartitionOffsets = new HashMap<>();
        final List<String> consumerGroupList = new ArrayList<>(topicPartitionList.size());

        for (final TopicPartition topicPartition : topicPartitionList) {
            topicPartitionOffsets.put(topicPartition, new OffsetSpec());
            consumerGroupList.add(KafkaUtils.toConsumerGroupName(topicPartition.topic()));
        }

        final ListOffsetsResult listOffsetsResult = this.kafkaAdminClient.listOffsets(topicPartitionOffsets);
        final Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> topicPartitionListOffsetsResultInfoMap = listOffsetsResult.all().get();

        for (final Map.Entry<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> pair : topicPartitionListOffsetsResultInfoMap.entrySet()) {
            TopicOffset topicOffset = new TopicOffset();
            topicOffset.setSize(pair.getValue().offset());
            result.put(pair.getKey(), topicOffset);
        }

        for (final String consumerGroup : consumerGroupList) {
            final ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = this.kafkaAdminClient.listConsumerGroupOffsets(consumerGroup);
            final Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().get();

            for (final Map.Entry<TopicPartition, TopicOffset> pair : result.entrySet()) {
                if (topicPartitionOffsetAndMetadataMap.containsKey(pair.getKey())) {
                    final OffsetAndMetadata offsetAndMetadata = topicPartitionOffsetAndMetadataMap.get(pair.getKey());
                    pair.getValue().setOffset(offsetAndMetadata.offset());
                    pair.getValue().setOffsetTimestamp(offsetAndMetadata.metadata());
                    if (offsetAndMetadata.leaderEpoch().isPresent()) {
                        pair.getValue().setLeaderEpoch(offsetAndMetadata.leaderEpoch().get());
                    }
                    break;
                }
            }
        }

        return result;
    }

    public final TopicOffset listOffsetForTimes(final TopicPartition topicPartition,
                                                final long timestamp) throws ExecutionException, InterruptedException {
        final TopicOffset result = new TopicOffset();
        final Map<TopicPartition, OffsetSpec> topicPartitionOffsets = new HashMap<>();
        topicPartitionOffsets.put(topicPartition, OffsetSpec.forTimestamp(timestamp));
        final ListOffsetsResult listOffsetsResult = this.kafkaAdminClient.listOffsets(topicPartitionOffsets);
        final Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> topicPartitionListOffsetsResultInfoMap = listOffsetsResult.all().get();

        for (final Map.Entry<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> pair : topicPartitionListOffsetsResultInfoMap.entrySet()) {
            final ListOffsetsResult.ListOffsetsResultInfo value = pair.getValue();
            result.setOffset(value.offset());
            result.setOffsetTimestamp(String.valueOf(value.timestamp()));
            if (value.leaderEpoch().isPresent()) {
                result.setLeaderEpoch(value.leaderEpoch().get());
            }
        }
        return result;
    }

    @Override
    public final void close() {
        if (null != this.kafkaAdminClient) {
            this.kafkaAdminClient.close();
        }
    }
}