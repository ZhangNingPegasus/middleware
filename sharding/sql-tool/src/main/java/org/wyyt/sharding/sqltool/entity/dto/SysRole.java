package org.wyyt.sharding.sqltool.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.admin.ui.entity.base.BaseDto;

/**
 * The entity of role
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
public final class SysRole extends BaseDto {
    @TableField(value = "name")
    private String name;
    @TableField(value = "super_admin")
    private Boolean superAdmin;
    @TableField(value = "remark")
    private String remark;
}