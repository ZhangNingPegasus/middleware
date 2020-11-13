package org.wyyt.db2es.client.processor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.util.ObjectUtils;
import org.wyyt.db2es.client.common.CheckpointExt;
import org.wyyt.db2es.client.common.Context;
import org.wyyt.db2es.client.common.RecordListener;
import org.wyyt.db2es.client.core.ConsumerRunner;
import org.wyyt.db2es.client.core.RecordListenerImpl;
import org.wyyt.db2es.client.core.RecordRunner;
import org.wyyt.db2es.client.entity.Processor;
import org.wyyt.db2es.client.kafka.KafkaConsumerWrapperFactory;
import org.wyyt.db2es.client.thread.WorkerThread;
import org.wyyt.db2es.core.entity.domain.Names;
import org.wyyt.db2es.core.entity.persistent.Topic;
import org.wyyt.db2es.core.exception.Db2EsException;
import org.wyyt.db2es.core.util.kafka.KafkaUtils;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.exception.ExceptionTool;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * the wapper class of processor with RecordRunner and ConsumerRunner
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class ProcessorWrapper implements Closeable {
    private final Context context;
    private final AtomicBoolean isClosed;
    private final Map<TopicPartition, Processor> processorMap;

    public ProcessorWrapper(final Context context) {
        this.context = context;
        this.processorMap = new HashMap<>(32);
        this.isClosed = new AtomicBoolean(false);
    }

    public final void startAll(@Nullable CheckpointExt checkpoint) throws Exception {
        if (this.isClosed.get()) {
            return;
        }

        log.info(String.format("found [%s] topics for db2es_id[%s], %s",
                this.context.getConfig().getTopicMap().size(),
                this.context.getConfig().getDb2EsId(),
                this.context.getConfig().getTopicMap().values().stream().map(Topic::getName).collect(Collectors.toList())));

        for (final Topic topic : this.context.getConfig().getTopicMap().values()) {
            final int partition = 0;

            if (null == checkpoint) {
                checkpoint = getCheckpointByProperties(topic.getName(), partition);
            }

            this.startTopic(
                    new TopicPartition(topic.getName(), partition),
                    checkpoint,
                    new RecordListenerImpl(this.context));
        }
    }

    public final boolean stopAll() throws InterruptedException {
        if (!this.processorMap.isEmpty()) {
            final List<TopicPartition> needRemove = new ArrayList<>(this.processorMap.size());
            final CountDownLatch cdl = new CountDownLatch(this.processorMap.size());
            for (final Map.Entry<TopicPartition, Processor> pair : this.processorMap.entrySet()) {
                final TopicPartition topicPartition = pair.getKey();
                final Processor processor = pair.getValue();
                new Thread(() -> {
                    try {
                        processor.stop();
                        needRemove.add(topicPartition);
                    } catch (final InterruptedException exception) {
                        log.error(String.format("ProcessorWrapper: stop Processor meet error, %s", ExceptionTool.getRootCauseMessage(exception)), exception);
                    } finally {
                        cdl.countDown();
                    }
                }).start();
            }
            cdl.await();

            for (final TopicPartition topicPartition : needRemove) {
                this.processorMap.remove(topicPartition);
            }
        }
        return 0 == this.processorMap.size();
    }

    public final Processor startTopic(final TopicPartition topicPartition,
                                      @Nullable final CheckpointExt checkpoint,
                                      final RecordListener recordListener) throws Exception {
        if (this.isClosed.get()) {
            return null;
        }

        if (!this.context.getConfig().getTopicMap().containsKey(topicPartition.topic())) {
            throw new Db2EsException(String.format("[db2es.id=%s]没有安装主题[%s]",
                    this.context.getConfig().getDb2EsId(),
                    topicPartition.topic()));
        }

        if (this.processorMap.containsKey(topicPartition)) {
            throw new Db2EsException(String.format("Topic[%s]已存在, 请先关闭, 在添加", topicPartition.topic()));
        }

        final Topic topic = this.context.getConfig().getTopicMap().get(topicPartition.topic());
        this.context.getElasticSearchWrapper().refreshTopicSuffix(topic);

        final Processor result = new Processor();
        final String indexName = this.context.getElasticSearchWrapper().getIndexName(
                topicPartition.topic(),
                Calendar.getInstance().getTime(),
                false);
        log.info(String.format("index[%s] for topic[%s] is ready right now", indexName, topicPartition));

        final RecordRunner recordRunner = new RecordRunner(result,
                this.context,
                topicPartition.topic(),
                topicPartition.partition(),
                KafkaUtils.toConsumerGroupName(topicPartition.topic()),
                checkpoint,
                new KafkaConsumerWrapperFactory());

        final ConsumerRunner consumerRunner = new ConsumerRunner(this.context,
                recordRunner,
                (toCommitCheckpoint) -> {
                    recordRunner.setToCommitCheckpoint(toCommitCheckpoint);
                    return toCommitCheckpoint;
                },
                recordListener);

        recordRunner.setConsumerRunner(consumerRunner);
        result.setTopicPartition(topicPartition);
        result.setRecordThread(new WorkerThread(recordRunner, String.format("thread-record-for-topic-%s", recordRunner.getTopicPartition())));
        result.setConsumerThread(new WorkerThread(consumerRunner, String.format("thread-consumer-for-topic-%s", consumerRunner.getRecordRunner().getTopicPartition())));
        this.processorMap.put(topicPartition, result);
        result.start();
        return result;
    }

    public final void stopTopic(final TopicPartition topicPartition) throws InterruptedException {
        if (this.processorMap.containsKey(topicPartition)) {
            this.processorMap.get(topicPartition).stop();
            this.processorMap.remove(topicPartition);
            log.info(String.format("ProcessorWrapper: Topic[%s] stopped working at %s", topicPartition, DateTool.format(new Date())));
        }
    }

    public final int processorSize() {
        return this.processorMap.size();
    }

    public final boolean containsTopic(final TopicPartition topicPartition) {
        return this.processorMap.containsKey(topicPartition);
    }

    public final Processor getByTopicPartition(final TopicPartition topicPartition) {
        return this.processorMap.get(topicPartition);
    }

    public final Set<String> listTopics() {
        return this.processorMap.keySet().stream().map(TopicPartition::topic).collect(Collectors.toSet());
    }

    @SneakyThrows
    @Override
    public final void close() {
        this.isClosed.set(true);
        stopAll();
    }

    private CheckpointExt getCheckpointByProperties(final String topicName,
                                                    final int partition) {
        String checkpointExpr = this.context.getConfig().getTopicCheckpointMap().get(String.format(Names.TOPIC_CHECKPOINT_FORMAT, topicName, partition));
        if (ObjectUtils.isEmpty(checkpointExpr)) {
            checkpointExpr = this.context.getConfig().getInitialCheckpoint();
        }
        if (ObjectUtils.isEmpty(checkpointExpr)) {
            return null;
        }
        return parseCheckpoint(checkpointExpr);
    }

    private static CheckpointExt parseCheckpoint(final String checkpointExpr) {
        if (ObjectUtils.isEmpty(checkpointExpr)) {
            return null;
        }
        final String[] offsetAndTS = checkpointExpr.split("@");
        CheckpointExt result;
        if (1 == offsetAndTS.length) {
            result = new CheckpointExt(null, Long.parseLong(offsetAndTS[0]), -1);
        } else if (2 == offsetAndTS.length) {
            result = new CheckpointExt(null, Long.parseLong(offsetAndTS[0]), Long.parseLong(offsetAndTS[1]));
        } else {
            throw new Db2EsException(String.format("位点表达式[%s]不符合格式要求. 正确格式: [偏移量] 或 [偏移量@消费位点的时间戳], 例如: 1183 或 1183@1591752301558, 当时后者时，只会根据时间戳进行消费位点的重置", checkpointExpr));
        }
        return result;
    }
}