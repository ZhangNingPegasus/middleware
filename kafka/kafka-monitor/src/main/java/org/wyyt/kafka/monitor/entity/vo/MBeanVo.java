package org.wyyt.kafka.monitor.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

/**
 * The view object for ajax's response. Using for show the kafka's JMX's MBean information.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class MBeanVo implements Serializable {
    private String name;
    private String oneMinute;
    private String fiveMinute;
    private String fifteenMinute;
    private String meanRate;
    private Double dblOneMinute;
    private Double dblFiveMinute;
    private Double dblFifteenMinute;
    private Double dblMeanRate;
}