package org.wyyt.kafka.monitor.entity.echarts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * The view object for cpu information.
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
public class CpuInfo implements Serializable {
    private List<String> xAxis;
    private List<Double> systemCpu;
    private List<Double> processCpu;

    private String strXAxis;
    private String strSystemCpu;
    private String strProcessCpu;
}