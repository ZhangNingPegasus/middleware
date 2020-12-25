package org.wyyt.kafka.monitor.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * The view object for ajax's response. Using for show the kafka's topics information.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class TopicVo implements Serializable {
    private String topicName;
    private Integer partitionNum;
    private Integer consumerStatus;
    private Long lag;
    private Long logSize;
    private Long day0LogSize;
    private Long day1LogSize;
    private Long day2LogSize;
    private Long day3LogSize;
    private Long day4LogSize;
    private Long day5LogSize;
    private Long day6LogSize;
    private Integer subscribeNums;
    private String[] subscribeGroupIds;
    private String partitionIndex;
    private Date createTime;
    private Date modifyTime;
    private String error;
}