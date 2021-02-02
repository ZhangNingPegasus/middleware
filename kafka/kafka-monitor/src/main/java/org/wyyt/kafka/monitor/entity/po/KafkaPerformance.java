package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;
import org.wyyt.kafka.monitor.entity.echarts.LineInfo;

/**
 * the entity class for offset of kafka performance
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
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