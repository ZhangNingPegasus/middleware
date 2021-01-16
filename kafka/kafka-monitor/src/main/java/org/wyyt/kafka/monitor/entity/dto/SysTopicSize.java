package org.wyyt.kafka.monitor.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.wyyt.admin.ui.entity.base.BaseDto;

/**
 * The entity for table `sys_topic_size`. Using for saving the kafka topics' log size.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "`sys_topic_size`")
public class SysTopicSize extends BaseDto {
    /**
     * 主题名称
     */
    @TableField(value = "`topic_name`")
    private String topicName;

    /**
     * 主题对应的信息数量
     */
    @TableField(value = "`log_size`")
    private Long logSize;
}