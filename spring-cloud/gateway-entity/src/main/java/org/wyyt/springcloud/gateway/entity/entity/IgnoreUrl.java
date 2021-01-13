package org.wyyt.springcloud.gateway.entity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.springcloud.gateway.entity.entity.base.BaseDto;

/**
 * The entity of table `t_ignore_url`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`t_ignore_url`")
public class IgnoreUrl extends BaseDto {
    @TableField(value = "`url`")
    private String url;

    @TableField(value = "`description`")
    private String description;
}