package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;
import org.wyyt.kafka.monitor.entity.echarts.LineInfo;

/**
 * the entity class for offset of zookeeper performance
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class ZooKeeperPerformance {
    private LineInfo send;
    private LineInfo received;
    private LineInfo alive;
    private LineInfo queue;
}
