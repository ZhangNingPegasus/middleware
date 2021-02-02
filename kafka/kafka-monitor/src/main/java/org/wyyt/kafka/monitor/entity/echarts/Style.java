package org.wyyt.kafka.monitor.entity.echarts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

/**
 * The entity for ajax's response. Using for echart's style.
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
public class Style implements Serializable {
    private String color;
    private String borderColor;

    private Style(String color, String borderColor) {
        this.color = color;
        this.borderColor = borderColor;
    }

    public static Style warn() {
        return new Style("#FFB800", "#FFB800");
    }

    public static Style success() {
        return new Style("#009688", "#009688");
    }

    public static Style info() {
        return new Style("#cccccc", "#cccccc");
    }
}