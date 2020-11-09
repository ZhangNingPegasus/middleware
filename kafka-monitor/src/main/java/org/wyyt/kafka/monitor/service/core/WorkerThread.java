package org.wyyt.kafka.monitor.service.core;

import lombok.extern.slf4j.Slf4j;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.resource.ResourceTool;

/**
 * manage the thread which used for running some jobs.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class WorkerThread<T extends Runnable & AutoCloseable> {
    private final String threadName;
    private final T runner;
    private final Thread worker;

    public WorkerThread(final T runner,
                        final String threadName) {
        this.runner = runner;
        this.threadName = threadName;
        this.worker = new Thread(runner);
        this.worker.setName(threadName);
    }

    public final void start() {
        this.worker.start();
    }

    public final Thread.State state() {
        return this.worker.getState();
    }

    public final void stop() {
        if (this.runner instanceof AutoCloseable) {
            ResourceTool.closeQuietly(this.runner);
        }
        try {
            this.worker.join(10000, 0);
        } catch (InterruptedException exception) {
            log.info(ExceptionTool.getRootCauseMessage(exception), exception);
            Thread.currentThread().interrupt();
        }
    }
}