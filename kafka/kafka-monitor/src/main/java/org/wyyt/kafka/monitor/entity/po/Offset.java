package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;

/**
 * the entity class for offset of topic
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class Offset {
    private String topicName;
    private Integer partitionId;
    private Long offset;
    private String metadata;
}