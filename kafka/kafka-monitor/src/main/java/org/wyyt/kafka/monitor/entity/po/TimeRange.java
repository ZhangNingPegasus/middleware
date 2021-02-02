package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;

import java.util.Date;

/**
 * the entity class for time range
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class TimeRange {
    private Date start;
    private Date end;
}