package org.wyyt.sharding.db2es.admin.rebuild;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The service for Semapthore
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class ThreadService implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(ThreadService.class);
    private static final Integer MAX_CONCURRENCY_NUMBER = 10;
    private final ExecutorService executorService;
    private final ArrayBlockingQueue<Runnable> blockingQueue;

    public final void submit(final Runnable task) {
        this.executorService.submit(task);
    }

    public final int getWaitingSize() {
        return this.blockingQueue.size();
    }

    public ThreadService() {
        this.blockingQueue = new ArrayBlockingQueue<>(128);
        this.executorService = new ThreadPoolExecutor(0,
                MAX_CONCURRENCY_NUMBER,
                1000L,
                TimeUnit.MILLISECONDS,
                this.blockingQueue,
                new ThreadFactoryBuilder()
                        .setNameFormat("pool-thread-for-rebuild-index-%d")
                        .setDaemon(false)
                        .setUncaughtExceptionHandler((thread, exception) -> logger.error(String.format("线程池异常, 原因: %s", exception.getMessage()), exception))
                        .build(),
                (runnable, executor) -> {
                    throw new RuntimeException(String.format("线程池已满. 线程[%s]被拒绝", runnable.toString()));
                });
    }

    @Override
    public final void destroy() throws Exception {
        this.executorService.shutdown();
        this.executorService.awaitTermination(10, TimeUnit.MINUTES);
    }
}