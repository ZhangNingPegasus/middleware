package org.wyyt.gateway.admin.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.gateway.admin.business.entity.base.BaseDto;

/**
 * The entity of table `t_gray`
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
@TableName(value = "`t_gray`")
public class Gray extends BaseDto {
    @TableField(value = "`gray_id`")
    private String grayId;

    @TableField(value = "`description`")
    private String description;
}