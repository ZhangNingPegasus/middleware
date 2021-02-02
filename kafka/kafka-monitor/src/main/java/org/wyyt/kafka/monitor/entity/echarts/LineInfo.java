package org.wyyt.kafka.monitor.entity.echarts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * The entity for ajax's response. Using for echart's line graph.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LineInfo implements Serializable {
    private List<String> topicNames;
    private List<String> times;
    private List<Series> series;
}