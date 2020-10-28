package org.wyyt.db2es.core.entity.persistent;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * The entity for table t_table
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
@TableName(value = "`t_table`")
public final class Table extends BaseDto {
    private static final long serialVersionUID = 1L;
    /**
     * 表信息
     */
    @TableField(value = "`info`")
    private String info;
}