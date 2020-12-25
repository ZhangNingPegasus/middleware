package org.wyyt.sharding.db2es.admin.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.sharding.db2es.core.entity.persistent.BaseDto;

/**
 * The entity for table `error_log`
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`t_error_log`")
public final class ErrorLog extends BaseDto {
    private static final long serialVersionUID = 1L;

    /**
     * 主键值
     */
    @TableField(value = "`primary_key_value`")
    private String primaryKeyValue;

    /**
     * 数据库名称
     */
    @TableField(value = "`database_name`")
    private String databaseName;

    /**
     * 数据库表名称
     */
    @TableField(value = "`table_name`")
    private String tableName;

    /**
     * 索引名称
     */
    @TableField(value = "`index_name`")
    private String indexName;

    /**
     * 异常消息
     */
    @TableField(value = "`error_message`")
    private String errorMessage;

    /**
     * kafka消费失败的原始内容
     */
    @TableField(value = "`consumer_record`")
    private String consumerRecord;

    /**
     * kafka消息所在的主题
     */
    @TableField(value = "`topic_name`")
    private String topicName;

    /**
     * kakfa消息所在的主题分区
     */
    @TableField(value = "`partition`")
    private Integer partition;

    /**
     * kafka消息的偏移量
     */
    @TableField(value = "`offset`")
    private Long offset;

    /**
     * 是否已解决(0: 否; 1:是)
     */
    @TableField(value = "`is_resolved`")
    private Integer isResolved;
}