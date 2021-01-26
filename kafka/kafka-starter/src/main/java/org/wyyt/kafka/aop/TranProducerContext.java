package org.wyyt.kafka.aop;

import org.apache.kafka.clients.producer.Producer;

/**
 * The thread-local variable used for storing kafkaConsumer of transaction.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public class TranProducerContext {
    private static final ThreadLocal<Producer<String, String>> CONTEXT = ThreadLocal.withInitial(() -> null);

    public static Producer<String, String> get() {
        return CONTEXT.get();
    }

    public static void set(final Producer<String, String> producer) {
        CONTEXT.set(producer);
    }

    public static void clear() {
        CONTEXT.remove();
    }
}