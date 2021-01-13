package org.wyyt.springcloud.gateway.entity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.springcloud.gateway.entity.entity.base.BaseDto;

/**
 * The entity of table `t_auth`
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
@TableName(value = "`t_auth`")
public class Auth extends BaseDto {
    @TableField(value = "`app_id`")
    private String appId;

    @TableField(value = "`api_id`")
    private String apiId;
}