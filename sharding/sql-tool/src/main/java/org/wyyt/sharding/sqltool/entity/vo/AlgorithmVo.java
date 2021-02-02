package org.wyyt.sharding.sqltool.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.ToString;

/**
 * The view object of algorithm
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@ToString
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public final class AlgorithmVo {
    private String hash;
    private String database;
    private String table;
}