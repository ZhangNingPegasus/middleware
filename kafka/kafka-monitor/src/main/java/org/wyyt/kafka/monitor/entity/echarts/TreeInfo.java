package org.wyyt.kafka.monitor.entity.echarts;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * The entity for ajax's response. Using for echart's tree graph.
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
public class TreeInfo implements Serializable {
    private String name;
    private Integer value;
    private Style itemStyle;
    private Style lineStyle;
    private List<TreeInfo> children;

    public TreeInfo(String name) {
        this.name = name;
    }

    public void setStyle(Style style) {
        this.setItemStyle(style);
        this.setLineStyle(style);
    }
}
