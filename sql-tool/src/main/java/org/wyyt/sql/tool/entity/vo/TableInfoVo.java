package org.wyyt.sql.tool.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wyyt.sharding.entity.FieldInfo;
import org.wyyt.sharding.entity.IndexInfo;

import java.io.Serializable;
import java.util.List;

/**
 * The view object of table information
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public final class TableInfoVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer databaseCount;
    private Integer tableCount;
    private List<IndexInfo> index;
    private List<FieldInfo> field;
}