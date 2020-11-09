package org.wyyt.kafka.monitor.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.kafka.monitor.entity.base.BaseDto;

/**
 * The entity for table `sys_topic_lag`. Using for saving how many messages are backlogged.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`sys_topic_lag`")
public class SysTopicLag extends BaseDto {
    /**
     * 消费者名称
     */
    @TableField(value = "`group_id`")
    private String groupId;

    /**
     * 消费者订阅的主题名称
     */
    @TableField(value = "`topic_name`")
    private String topicName;

    /**
     * 当前消费的偏移量位置
     */
    @TableField(value = "`offset`")
    private Long offset;

    /**
     * 消息堆积数量
     */
    @TableField(value = "`lag`")
    private Long lag;
}