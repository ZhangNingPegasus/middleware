package org.wyyt.springcloud.gateway.entity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.springcloud.gateway.entity.entity.base.BaseDto;

/**
 * The entity of table `t_gray`
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
@TableName(value = "`t_gray`")
public class Gray extends BaseDto {
    @TableField(value = "`gray_id`")
    private String grayId;

    @TableField(value = "`description`")
    private String description;
}