package org.wyyt.springcloud.gateway.entity.entity.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * All DTO class's super class.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public abstract class BaseDto implements Serializable {
    /**
     * 自增主键
     */
    @TableId(value = "`id`", type = IdType.AUTO)
    private Long id;

    /**
     * 记录创建时间
     */
    @TableField(value = "`row_create_time`")
    private Date rowCreateTime;

    /**
     * 最后一次修改时间
     */
    @TableField(value = "`row_update_time`")
    private Date rowUpdateTime;
}