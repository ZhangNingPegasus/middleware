package org.wyyt.admin.ui.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * The entity of time range
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
public final class TimeRangeVo {
    private Date start;
    private Date end;
}