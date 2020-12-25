package org.wyyt.kafka.monitor.entity.echarts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * The entity for ajax's response. Using for echart's series.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Series implements Serializable {
    private String name;
    private List<Double> data;
    private String type;
    private Boolean smooth;
    private JSONObject areaStyle;
}