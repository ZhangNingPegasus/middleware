package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;

/**
 * the entity class for KPI of zookeeper.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class ZooKeeperKpi {
    private String zkPacketsReceived;// received client packet numbers
    private String zkPacketsSent;// send client packet numbers
    private String zkAvgLatency;// response client request avg time
    private String zkNumAliveConnections;// has connected client numbers
    private String zkOutstandingRequests; //waiting deal with client request numbers in queue.
    private String zkOpenFileDescriptorCount; //server mode,like standalone|cluster[leader,follower].
    private String zkMaxFileDescriptorCount;
}
