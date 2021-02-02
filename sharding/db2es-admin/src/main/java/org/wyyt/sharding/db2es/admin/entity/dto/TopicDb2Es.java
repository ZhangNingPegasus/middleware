package org.wyyt.sharding.db2es.admin.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.sharding.db2es.core.entity.persistent.BaseDto;

/**
 * The entity for table `t_topic_db2es`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`t_topic_db2es`")
public final class TopicDb2Es extends BaseDto {
    private static final long serialVersionUID = 1L;

    /**
     * db2es_server的分布式id
     */
    @TableField(value = "`db2es_id`")
    private Integer db2esId;

    /**
     * 数据表t_topic的主键id
     */
    @TableField(value = "`topic_id`")
    private Long topicId;
}