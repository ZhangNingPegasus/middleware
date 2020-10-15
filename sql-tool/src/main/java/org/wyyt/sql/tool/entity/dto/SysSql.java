package org.wyyt.sql.tool.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.ToString;
import org.wyyt.tool.date.DateTool;

import java.io.Serializable;
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
@ToString
@Data
public final class SysSql implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
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
    @TableField(value = "create_time")
    private Date createTime;

    public String getStrExecutionTime() {
        return DateTool.format(executionTime);
    }
}