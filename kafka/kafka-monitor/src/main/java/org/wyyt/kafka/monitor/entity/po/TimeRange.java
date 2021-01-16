package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;

import java.util.Date;

/**
 * the entity class for time range
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class TimeRange {
    private Date start;
    private Date end;
}