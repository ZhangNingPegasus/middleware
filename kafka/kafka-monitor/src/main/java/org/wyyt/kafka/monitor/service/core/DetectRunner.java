package org.wyyt.kafka.monitor.service.core;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.TopicRecordService;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * the thread used for detecting the changes of kafka topic
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class DetectRunner extends BaseRunner implements AutoCloseable {
    private final KafkaService kafkaService;
    private final TopicRecordService topicRecordService;

    private Processor processor;
    private RecordRunner recordRunner;

    public DetectRunner(final KafkaService kafkaService,
                        final TopicRecordService topicRecordService) {
        this.kafkaService = kafkaService;
        this.topicRecordService = topicRecordService;
    }

    @Override
    public void run() {
        this.terminated = false;
        final Map<String, Set<Integer>> kafkaTopicMap = new HashMap<>(Constants.INIT_TOPIC_COUNT);
        final Map<String, Set<Integer>> currentTopicMap = new HashMap<>(Constants.INIT_TOPIC_COUNT);

        while (this.continued()) {
            try {
                if (null == this.processor) {
                    this.initProcess();
                } else {
                    kafkaTopicMap.clear();
                    kafkaTopicMap.putAll(this.kafkaService.listPartitionIds(this.kafkaService.listTopicNames()));

                    currentTopicMap.clear();
                    currentTopicMap.putAll(this.recordRunner.getTopicPartitionMap());

                    if (!JSON.toJSONString(kafkaTopicMap).equals(JSON.toJSONString(currentTopicMap))) {
                        this.initProcess();
                    }
                }
                this.sleepInterval(10000);
            } catch (final Exception e) {
                log.error(ExceptionTool.getRootCauseMessage(e), e);
            }
        }
    }

    public void initProcess() throws Exception {
        if (null != this.processor) {
            this.processor.stop();
            this.processor = null;
            this.recordRunner = null;
        }

        this.processor = new Processor();
        this.recordRunner = new RecordRunner(this.kafkaService, this.topicRecordService);
        final ConsumerRunner consumerRunner = new ConsumerRunner(this.topicRecordService);
        this.recordRunner.setConsumerRunner(consumerRunner);
        consumerRunner.setRecordRunner(this.recordRunner);

        this.processor.setRecordThread(new WorkerThread(this.recordRunner, "thread-record-for-topics"));
        this.processor.setConsumerThread(new WorkerThread(consumerRunner, "thread-consumer-for-topics"));
        this.processor.start();
    }

    @Override
    public void close() {
        try {
            super.close();
            this.processor.stop();
            this.processor = null;
            this.recordRunner = null;
        } catch (final Exception exception) {
            log.error(ExceptionTool.getRootCauseMessage(exception), exception);
        }
    }
}