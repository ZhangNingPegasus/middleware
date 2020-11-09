package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;
import org.wyyt.kafka.monitor.entity.echarts.LineInfo;

/**
 * the entity class for offset of kafka performance
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
public class KafkaPerformance {
    private LineInfo msgIn;
    private LineInfo bytesIn;
    private LineInfo bytesOut;
    private LineInfo bytesRejected;
    private LineInfo failedFetchRequest;
    private LineInfo failedProduceRequest;
    private LineInfo produceMessageConversions;
    private LineInfo totalFetchRequests;
    private LineInfo totalProduceRequests;
    private LineInfo replicationBytesOut;
    private LineInfo replicationBytesIn;
    private LineInfo osFreeMemory;
}