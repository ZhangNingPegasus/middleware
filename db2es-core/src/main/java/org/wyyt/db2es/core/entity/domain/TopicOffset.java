package org.wyyt.db2es.core.entity.domain;

import lombok.Data;

/**
 * the domain entity of topic's offset
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public final class TopicOffset {
    private Long offset;
    private String offsetTimestamp;
    private Long size;
    private Integer leaderEpoch;
}