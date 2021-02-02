package org.wyyt.kafka.monitor.entity.vo;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * The view object for ajax's response. Using for show the kafka's topics' partitions information.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class PartitionVo implements Serializable {
    private String topicName;
    private String partitionId;
    private Long logSize;
    private PartionInfo leader;
    private List<PartionInfo> replicas;
    private List<PartionInfo> isr;
    private String strLeader;
    private String strReplicas;
    private String strIsr;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonSerialize
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class PartionInfo implements Serializable {
        private String partitionId;
        private String host;
        private String port;
        private String rack;
    }
}