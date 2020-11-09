package org.wyyt.kafka.monitor.service.core;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.entity.dto.SysTableName;
import org.wyyt.kafka.monitor.entity.dto.TopicRecord;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.TopicRecordService;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.resource.ResourceTool;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * the thread used for polling the records from kafka server
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class RecordRunner extends BaseRunner {
    private static final int TRY_TIME = 150;
    private static final long INTERVAL = 100L;
    protected static final long TRY_BACK_TIME_MS = 10L * 1000L;
    private final KafkaService kafkaService;
    private final TopicRecordService topicRecordService;
    @Getter
    private final Map<String, Set<Integer>> topicPartitionMap;
    @Getter
    private Map<String, SysTableName> sysTableNameMap;
    @Setter
    private ConsumerRunner consumerRunner;

    public RecordRunner(final KafkaService kafkaService,
                        final TopicRecordService topicRecordService) {
        this.kafkaService = kafkaService;
        this.topicRecordService = topicRecordService;
        this.topicPartitionMap = new HashMap<>(Constants.INIT_TOPIC_COUNT);
    }

    @SneakyThrows
    @Override
    public final void run() {
        int offerTryCount;
        KafkaConsumerWrapper kafkaConsumerWrap = null;

        while (this.continued()) {
            try {
                kafkaConsumerWrap = this.getKafkaConsumerWrap();
                while (this.continued()) {
                    final ConsumerRecords<String, String> records = kafkaConsumerWrap.poll();
                    if (null == records || records.isEmpty()) {
                        continue;
                    }
                    for (final ConsumerRecord<String, String> record : records) {
                        if (null == record || null == record.value()) {
                            continue;
                        }
                        offerTryCount = 0;
                        final TopicRecord topicRecord = this.toTopicRecord(record);
                        while (!this.consumerRunner.offer(topicRecord, 1000, TimeUnit.MILLISECONDS)) {
                            if (++offerTryCount % 30 == 0) {
                                log.warn(String.format("Queue is full, the recod will be discarded, [%s]", record.value()));
                                break;
                            } else if (!this.continued()) {
                                break;
                            }
                        }
                    }
                }
            } catch (final Exception exception) {
                log.error(ExceptionTool.getRootCauseMessage(exception), exception);
                this.sleepInterval(TRY_BACK_TIME_MS);
            } finally {
                ResourceTool.closeQuietly(kafkaConsumerWrap);
            }
        }
    }

    private KafkaConsumerWrapper getKafkaConsumerWrap() throws Exception {
        this.topicPartitionMap.clear();
        this.topicPartitionMap.putAll(this.kafkaService.listPartitionIds(this.kafkaService.listTopicNames()));

        int count = (int) this.topicPartitionMap.values().stream().collect(Collectors.summarizingInt(Set::size)).getSum();
        final List<TopicPartition> topicPartitionList = new ArrayList<>(count);
        for (Map.Entry<String, Set<Integer>> pair : topicPartitionMap.entrySet()) {
            String topicName = pair.getKey();
            for (Integer partition : pair.getValue()) {
                topicPartitionList.add(new TopicPartition(topicName, partition));
            }
        }
        this.sysTableNameMap = this.topicRecordService.initTopicTable(topicPartitionList.stream().map(TopicPartition::topic).collect(Collectors.toSet()));
        final KafkaConsumerWrapper kafkaConsumerWrap = new KafkaConsumerWrapper(this.topicRecordService, this.kafkaService.getBootstrapServers(true), Constants.KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MESSAGE);
        kafkaConsumerWrap.assign(topicPartitionList);
        return kafkaConsumerWrap;
    }

    private TopicRecord toTopicRecord(final ConsumerRecord<String, String> record) {
        final TopicRecord topicRecord = new TopicRecord();
        topicRecord.setTopicName(record.topic());
        topicRecord.setPartitionId(record.partition());
        topicRecord.setOffset(record.offset());
        topicRecord.setKey((null == record.key()) ? "" : record.key());
        topicRecord.setValue((null == record.value()) ? "" : record.value());
        topicRecord.setTimestamp(new Date(record.timestamp()));
        return topicRecord;
    }
}