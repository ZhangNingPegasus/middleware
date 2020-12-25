package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;

/**
 * the entity class for offset of topic
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
public class Offset {
    private String topicName;
    private Integer partitionId;
    private Long offset;
    private String metadata;
}