package org.wyyt.kafka.monitor.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * the entity class for offset of topic partition
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class MaxOffset {
    @TableField(value = "`partition_id`")
    private Integer partitionId;

    @TableField(value = "`offset`")
    private Long offset;
}