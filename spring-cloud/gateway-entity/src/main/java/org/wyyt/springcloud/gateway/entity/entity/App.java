package org.wyyt.springcloud.gateway.entity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.springcloud.gateway.entity.entity.base.BaseDto;

/**
 * The entity of table `t_app`
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
@TableName(value = "`t_app`")
public class App extends BaseDto {
    @TableField(value = "`client_id`")
    private String clientId;

    @TableField(value = "`client_secret`")
    private String clientSecret;

    @TableField(value = "`name`")
    private String name;

    @TableField(value = "`is_admin`")
    private Boolean isAdmin;

    @TableField(value = "`description`")
    private String description;
}