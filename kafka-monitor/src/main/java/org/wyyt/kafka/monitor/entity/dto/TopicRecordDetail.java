package org.wyyt.kafka.monitor.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.kafka.monitor.entity.base.BaseDto;

/**
 * The entity for dynamic table. Using for saving the topic's full content.
 * One topic corresponds one table, the table's name is the name of topic.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class TopicRecordDetail extends BaseDto {
    /**
     * 分区id号
     */
    @TableField(value = "`partition_id`")
    private Integer partitionId;

    /**
     * 消息偏移量
     */
    @TableField(value = "`offset`")
    private Long offset;

    /**
     * 消息体
     */
    @TableField(value = "`value`")
    private String value;
}
