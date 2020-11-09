package org.wyyt.kafka.monitor.util;


import org.wyyt.tool.resource.ResourceTool;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;

/**
 * the utils class for Kafka's JMX.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public class JMXFactoryUtil {
    private static final ThreadFactory daemonThreadFactory = new DaemonThreadFactory();

    public static JMXConnector connectWithTimeout(final JMXServiceURL url,
                                                  final long timeout,
                                                  final TimeUnit unit) throws IOException {
        final BlockingQueue<Object> blockQueue = new ArrayBlockingQueue<>(1);
        final ExecutorService executor = Executors.newSingleThreadExecutor(daemonThreadFactory);
        executor.submit(() -> {
            try {
                final JMXConnector connector = JMXConnectorFactory.connect(url);
                if (!blockQueue.offer(connector)) {
                    ResourceTool.closeQuietly(connector);
                }
            } catch (final Throwable t) {
                blockQueue.offer(t);
            }
        });
        Object result;
        try {
            result = blockQueue.poll(timeout, unit);
            if (null == result) {
                if (!blockQueue.offer("")) {
                    result = blockQueue.take();
                }
            }
        } catch (final InterruptedException e) {
            throw initCause(new InterruptedIOException(e.getMessage()), e);
        } finally {
            executor.shutdown();
        }
        if (null == result)
            throw new SocketTimeoutException(String.format("Kafka JMX 连接超时: %s", url));
        if (result instanceof JMXConnector)
            return (JMXConnector) result;
        try {
            throw (Throwable) result;
        } catch (final Throwable e) {
            throw new IOException(e.toString(), e);
        }
    }

    private static <T extends Throwable> T initCause(final T wrapper,
                                                     final Throwable wrapped) {
        wrapper.initCause(wrapped);
        return wrapper;
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(final Runnable r) {
            final Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    }
}
