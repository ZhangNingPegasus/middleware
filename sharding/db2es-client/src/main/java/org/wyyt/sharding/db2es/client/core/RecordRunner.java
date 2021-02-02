package org.wyyt.sharding.db2es.client.core;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.wyyt.sharding.db2es.client.common.CheckpointExt;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.sharding.db2es.client.entity.Processor;
import org.wyyt.sharding.db2es.client.kafka.KafkaConsumerWrapper;
import org.wyyt.sharding.db2es.client.kafka.KafkaConsumerWrapperFactory;
import org.wyyt.tool.dingtalk.WarningLevel;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.resource.ResourceTool;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * the thread used for polling the records from kafka server
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class RecordRunner extends BaseRecordRunner {
    @Setter
    private ConsumerRunner consumerRunner;
    @Setter
    private volatile Exception consumeException;
    private volatile CheckpointExt toCommitCheckpoint = null;
    private final Processor processor;

    public RecordRunner(final Processor processor,
                        final Context context,
                        final String topicName,
                        final int partition,
                        final String groupName,
                        @Nullable final CheckpointExt initialCheckpoint,
                        final KafkaConsumerWrapperFactory kafkaConsumerWrapFactory) {
        super(context, topicName, partition, groupName, initialCheckpoint, kafkaConsumerWrapFactory);
        this.processor = processor;
    }

    @Override
    public final void run() {
        int haveTryTime = 0;
        int offerTryCount;
        KafkaConsumerWrapper kafkaConsumerWrap = null;

        while (this.continued()) {
            try {
                kafkaConsumerWrap = getKafkaConsumerWrap();
                this.toCommitCheckpoint = this.getInitialCheckpoint();
                while (this.continued()) {
                    this.throwConsumeException();
                    this.mayCommitCheckpoint();
                    final ConsumerRecords<String, String> records = kafkaConsumerWrap.poll();
                    if (null == records || records.isEmpty()) {
                        continue;
                    }
                    for (final ConsumerRecord<String, String> record : records) {
                        this.throwConsumeException();
                        offerTryCount = 0;
                        if (null == record || null == record.value()) {
                            continue;
                        }
                        while (!this.consumerRunner.offer(record, 1000, TimeUnit.MILLISECONDS)) {
                            this.throwConsumeException();
                            if (++offerTryCount % 30 == 0) {
                                log.warn(String.format("RecordRunner: topic[%s] offer record has failed for a period 30 seconds, [%s]", this.topicPartition, record.value()));
                                offerTryCount = 0;
                            }
                        }
                    }
                    haveTryTime = 0;
                }
            } catch (final Exception exception) {
                this.processor.setException(exception);
                if (!this.context.getConfig().getContinueOnError()) {
                    this.context.getDingDingWrapper().sendIfNoDuplicate(
                            String.format("在同步主题[%s]的消息过程中, 遭遇到错误, 会尝试持续重试, 直至成功为止: %s",
                                    this.topicPartition.topic(),
                                    ExceptionTool.getRootCauseMessage(exception)),
                            WarningLevel.WARNING);
                    this.sleepInterval(TRY_BACK_TIME_MS);
                } else if (haveTryTime < TRY_TIME) {
                    log.warn(String.format("RecordRunner: topic[%s] meet an error cause %s, recover time [%s]",
                            this.topicPartition,
                            ExceptionTool.getRootCauseMessage(exception),
                            haveTryTime), exception);
                    haveTryTime++;
                    this.sleepInterval(TRY_BACK_TIME_MS);
                } else {
                    log.error(String.format("RecordRunner: topic [%s] unrecoverable error %s, have try time [%s]",
                            this.topicPartition,
                            ExceptionTool.getRootCauseMessage(exception),
                            haveTryTime), exception);
                    this.context.getDingDingWrapper().sendIfNoDuplicate(String.format("主题[%s]已重试了[%s]次, 依旧失败, 因达到重试上限而停止该主题的消费线程, 请检查. 错误原因: [%s]",
                            this.topicPartition,
                            TRY_TIME,
                            ExceptionTool.getRootCauseMessage(exception)), WarningLevel.FATAL);
                    this.terminated = true;
                }
            } finally {
                ResourceTool.closeQuietly(kafkaConsumerWrap);
            }
        }
    }

    public final void setToCommitCheckpoint(final CheckpointExt checkpoint) {
        this.toCommitCheckpoint = checkpoint;
    }

    private void throwConsumeException() throws Exception {
        if (null == this.consumeException || this.context.getConfig().getContinueOnError()) {
            this.processor.setException(this.consumeException);
            return;
        }

        final Exception exception = new Exception(this.consumeException);
        this.consumeException = null;
        this.consumerRunner.clearQueue();
        throw exception;
    }

    private void mayCommitCheckpoint() {
        if (null != this.toCommitCheckpoint) {
            this.commitCheckpoint(this.toCommitCheckpoint.getTopicPartition(), this.toCommitCheckpoint);
            this.toCommitCheckpoint = null;
        }
    }

    private void commitCheckpoint(final TopicPartition topicPartition,
                                  final CheckpointExt checkpoint) {
        if (null != topicPartition && null != checkpoint) {
            this.metaStoreCenter.store(this.groupName, topicPartition, checkpoint);
        }
    }
}