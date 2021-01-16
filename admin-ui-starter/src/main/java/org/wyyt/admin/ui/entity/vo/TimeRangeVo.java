package org.wyyt.admin.ui.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * The entity of time range
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class TimeRangeVo {
    private Date start;
    private Date end;
}