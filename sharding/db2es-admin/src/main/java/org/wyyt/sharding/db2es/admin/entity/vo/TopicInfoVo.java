package org.wyyt.sharding.db2es.admin.entity.vo;

import lombok.Data;

/**
 * The View Object for Topic.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
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