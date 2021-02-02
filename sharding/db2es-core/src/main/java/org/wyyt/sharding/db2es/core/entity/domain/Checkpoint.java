package org.wyyt.sharding.db2es.core.entity.domain;

import lombok.Getter;
import lombok.ToString;
import org.apache.kafka.common.TopicPartition;

/**
 * the domain entity of check point of Kafka
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@ToString
@Getter
public class Checkpoint {
    protected final TopicPartition topicPartition;
    protected final long timestamp;
    protected final long offset;

    public Checkpoint(final TopicPartition topicPartition,
                      final long offset,
                      final long timestamp
    ) {
        this.topicPartition = topicPartition;
        this.offset = offset;
        this.timestamp = timestamp;
    }
}