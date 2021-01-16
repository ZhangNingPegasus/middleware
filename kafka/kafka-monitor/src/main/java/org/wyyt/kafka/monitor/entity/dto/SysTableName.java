package org.wyyt.kafka.monitor.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.wyyt.admin.ui.entity.base.BaseDto;

import java.util.Objects;

/**
 * The entity for table sys_table_name. Using for saving information of administrator.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
@TableName(value = "`sys_table_name`")
public class SysTableName extends BaseDto {
    /**
     * 主题名称
     */
    @TableField(value = "`topic_name`")
    private String topicName;

    /**
     * 主题对应的数据表名称(存储基本信息)
     */
    @TableField(value = "`record_table_name`")
    private String recordTableName;

    /**
     * 主题对应的数据表名称(存储消息体)
     */
    @TableField(value = "`record_detail_table_name`")
    private String recordDetailTableName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SysTableName that = (SysTableName) o;
        return Objects.equals(topicName, that.topicName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), topicName);
    }
}