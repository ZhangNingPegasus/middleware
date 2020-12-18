package org.wyyt.sql.tool.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.wyyt.admin.ui.entity.base.BaseDto;

/**
 * The entity of adminsitrator
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public final class SysAdmin extends BaseDto {
    @TableField(value = "sys_role_id")
    private Long sysRoleId;
    @TableField(value = "role_name")
    private String roleName;
    @TableField(value = "username")
    private String username;
    @TableField(value = "password")
    private String password;
    @TableField(value = "name")
    private String name;
    @TableField(value = "phoneNumber")
    private String phoneNumber;
    @TableField(value = "email")
    private String email;
    @TableField(value = "remark")
    private String remark;
}