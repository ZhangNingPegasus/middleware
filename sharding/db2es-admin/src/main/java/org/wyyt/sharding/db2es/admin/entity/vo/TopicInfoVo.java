package org.wyyt.sharding.db2es.admin.entity.vo;

import lombok.Data;

/**
 * The View Object for Topic.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class TopicInfoVo {
    private Integer num;
    private Integer db2esId;
    private String host;
    private String topicName;
    private Long size;
    private Long offset;
    private Long lag;
    private Integer tps;
    private String offsetDateTime;
    private Boolean isActive;
    private String errorMsg;
    private Long docsCount;
    private Boolean isOptimize;
    private String version;
}