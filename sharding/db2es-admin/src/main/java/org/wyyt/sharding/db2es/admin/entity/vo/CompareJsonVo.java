package org.wyyt.sharding.db2es.admin.entity.vo;

import lombok.Data;

/**
 * The View Object for result of JSON compare
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class CompareJsonVo {
    private String first;
    private String second;
}