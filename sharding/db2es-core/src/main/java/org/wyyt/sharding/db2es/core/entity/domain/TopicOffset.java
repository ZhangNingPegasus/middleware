package org.wyyt.sharding.db2es.core.entity.domain;

import lombok.Data;

/**
 * the domain entity of topic's offset
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class TopicOffset {
    private Long offset;
    private String offsetTimestamp;
    private Long size;
    private Integer leaderEpoch;
}