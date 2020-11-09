package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;
import org.wyyt.kafka.monitor.entity.echarts.LineInfo;

/**
 * the entity class for offset of zookeeper performance
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
public class ZooKeeperPerformance {
    private LineInfo send;
    private LineInfo received;
    private LineInfo alive;
    private LineInfo queue;
}
