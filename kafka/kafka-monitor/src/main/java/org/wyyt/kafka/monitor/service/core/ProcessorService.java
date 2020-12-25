package org.wyyt.kafka.monitor.service.core;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.TopicRecordService;

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
@Service
public class ProcessorService implements SmartLifecycle, DisposableBean {
    private final DetectRunner detectRunner;
    private volatile boolean running;
    private WorkerThread workerThread;

    public ProcessorService(@Lazy final KafkaService kafkaService,
                            @Lazy final TopicRecordService topicRecordService) {
        this.detectRunner = new DetectRunner(kafkaService, topicRecordService);
    }

    @SneakyThrows
    @Override
    public void start() {
        this.workerThread = new WorkerThread(this.detectRunner, "thread-new-topic-detect");
        this.workerThread.start();
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
        if (null != this.workerThread) {
            this.workerThread.stop();
            this.workerThread = null;
        }
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void destroy() {
        this.stop();
    }
}