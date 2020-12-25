package org.wyyt.kafka.monitor.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * the entity class for offset of topic partition
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
public class MaxOffset {
    @TableField(value = "`partition_id`")
    private Integer partitionId;

    @TableField(value = "`offset`")
    private Long offset;
}