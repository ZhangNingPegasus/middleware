package org.wyyt.db2es.client.kafka;

import org.apache.kafka.common.KafkaException;

/**
 * the business exception occured on cluster switch
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public final class ClusterSwitchException extends KafkaException {
    public ClusterSwitchException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ClusterSwitchException(final String message) {
        super(message);
    }

    public ClusterSwitchException(final Throwable cause) {
        super(cause);
    }

    public ClusterSwitchException() {
        super();
    }
}