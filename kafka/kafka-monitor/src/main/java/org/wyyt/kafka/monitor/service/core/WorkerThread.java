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
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class WorkerThread<T extends Runnable & AutoCloseable> {
    private final T runner;
    private final Thread worker;

    public WorkerThread(final T runner,
                        final String threadName) {
        this.runner = runner;
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
        if (null != this.runner) {
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