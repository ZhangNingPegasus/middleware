package org.wyyt.sharding.sqltool.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.wyyt.sharding.entity.FactSql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The view object of data table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public final class DataTableVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean goodSql = false;
    private List<FactSql> factSql;
    private List<String> columnNameList = new ArrayList<>();
    private List<List<String>> valueList = new ArrayList<>();
    private Long execTime;
}