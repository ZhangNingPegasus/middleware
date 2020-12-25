package org.wyyt.sharding.db2es.client.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.ClusterResource;
import org.apache.kafka.common.ClusterResourceListener;

import java.util.Map;

/**
 * When the Kafka cluster is in HA mode and a failover occurs, consumers may switch from one Kafka cluster to another.
 * Because the clusters are different, we need to monitor. At this time, you need to re-create the KafkaConsumer and
 * use the timestamp to restore to the last unconsumed position to continue consumption to minimize duplicate data.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class ClusterSwitchListener implements ClusterResourceListener, ConsumerInterceptor {
    private ClusterResource originClusterResource = null;

    @Override
    public ConsumerRecords onConsume(final ConsumerRecords consumerRecords) {
        return consumerRecords;
    }

    @Override
    public void close() {
    }

    @Override
    public void onCommit(final Map map) {
    }

    @Override
    public void onUpdate(final ClusterResource clusterResource) {
        synchronized (this) {
            if (null == originClusterResource) {
                this.originClusterResource = clusterResource;
            } else {
                if (!clusterResource.clusterId().equals(originClusterResource.clusterId())) {
                    throw new ClusterSwitchException(String.format("Cluster changed from %s to %s, consumer require restart",
                            originClusterResource.clusterId(),
                            clusterResource.clusterId()));
                }
            }
        }
    }

    @Override
    public void configure(final Map<String, ?> map) {
    }
}