package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;
import org.wyyt.kafka.monitor.entity.dto.SysTopicLag;
import org.wyyt.kafka.monitor.entity.dto.SysTopicSize;

import java.util.List;

/**
 * the entity class for kpi of topic
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
public class TopicSizeLag {
    private List<SysTopicSize> sysTopicSizeList;
    private List<SysTopicLag> sysTopicLagList;
}