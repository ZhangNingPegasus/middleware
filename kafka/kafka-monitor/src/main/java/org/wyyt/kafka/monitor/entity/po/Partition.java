package org.wyyt.kafka.monitor.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/**
 * the entity class for partition table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class Partition {
    @TableField(value = "`table_name`")
    private String tableName;

    @TableField(value = "`partition_name`")
    private String partitionName;

    @TableField(value = "`partition_expression`")
    private String partitionExpression;

    @TableField(value = "`description`")
    private String description;

    @TableField(value = "`table_rows`")
    private String tableRows;

    @TableField(exist = false)
    private Date date;
}