package org.wyyt.sharding.sqltool.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.wyyt.admin.ui.entity.base.BaseDto;
import org.wyyt.tool.date.DateTool;

import java.util.Date;

/**
 * The entity of SQL statement
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public final class SysSql extends BaseDto {

    @TableField(value = "sysAdminId")
    private Long sysAdminId;
    @TableField(value = "name")
    private String name;
    @TableField(value = "ip")
    private String ip;
    @TableField(value = "short_sql")
    private String shortSql;
    @TableField(value = "logic_sql")
    private String logicSql;
    @TableField(value = "fact_sql")
    private String factSql;
    @TableField(value = "execution_time")
    private Date executionTime;
    private String strExecutionTime;
    @TableField(value = "execution_duration")
    private Long executionDuration;

    public String getStrExecutionTime() {
        return DateTool.format(executionTime);
    }
}