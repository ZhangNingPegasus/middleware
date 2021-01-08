package org.wyyt.springcloud.util;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * The common functions
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public class Common {
    public static ThreadPoolTaskExecutor generateExecutor(final String prefixName) {
        final ThreadPoolTaskExecutor result = new ThreadPoolTaskExecutor();
        result.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        result.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 5);
        result.setQueueCapacity(Runtime.getRuntime().availableProcessors() * 2);
        result.setThreadNamePrefix(prefixName);
        result.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        result.initialize();
        return result;
    }
}
