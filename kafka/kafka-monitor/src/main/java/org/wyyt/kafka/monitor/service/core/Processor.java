package org.wyyt.kafka.monitor.service.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * the entity of processor for:
 * 1. poll records from kafka.
 * 2. consume kafka's records.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@NoArgsConstructor
@Data
public final class Processor {
    private WorkerThread<?> recordThread;
    private WorkerThread<?> consumerThread;

    public final void start() {
        Assert.isTrue(null != this.recordThread, "RecordThread is required");
        Assert.isTrue(null != this.consumerThread, "ConsumerThread is required");

        if (Thread.State.NEW == this.recordThread.state()) {
            this.recordThread.start();
        }
        if (Thread.State.NEW == this.consumerThread.state()) {
            this.consumerThread.start();
        }
    }

    public final void stop() throws InterruptedException {
        Assert.isTrue(null != this.recordThread, "RecordThread is required");
        Assert.isTrue(null != this.consumerThread, "ConsumerThread is required");

        final CountDownLatch cdl = new CountDownLatch(2);
        final List<WorkerThread<?>> workerThreads = Arrays.asList(this.recordThread, this.consumerThread);
        for (final WorkerThread<?> workerThread : workerThreads) {
            new Thread(() -> {
                try {
                    workerThread.stop();
                } finally {
                    cdl.countDown();
                }
            }).start();
        }
        cdl.await();
    }
}