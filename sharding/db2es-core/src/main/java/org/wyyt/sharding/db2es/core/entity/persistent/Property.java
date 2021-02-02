package org.wyyt.sharding.db2es.core.entity.persistent;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The entity for table t_property
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
@TableName(value = "`t_property`")
public final class Property extends BaseDto {
    private static final long serialVersionUID = 1L;

    /**
     * 配置项名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 配置项的值
     */
    @TableField(value = "`value`")
    private String value;

    /**
     * 配置项描述信息
     */
    @TableField(value = "`description`")
    private String description;
}