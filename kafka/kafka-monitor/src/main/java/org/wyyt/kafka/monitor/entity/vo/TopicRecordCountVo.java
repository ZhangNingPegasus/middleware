package org.wyyt.kafka.monitor.entity.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * The View Object for topic record size.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
public class TopicRecordCountVo {
    @TableField(value = "`topic_name`")
    private String topicName;

    @TableField(value = "`log_size`")
    private Long logSize;

    @TableField(value = "`growth_rate`")
    private Double growthRate;
}