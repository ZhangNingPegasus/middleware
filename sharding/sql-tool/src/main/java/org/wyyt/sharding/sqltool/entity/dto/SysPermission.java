package org.wyyt.sharding.sqltool.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.wyyt.admin.ui.entity.base.BaseDto;

/**
 * The entity of permission
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public final class SysPermission extends BaseDto {
    @TableField(value = "sys_role_id")
    private Long sysRoleId;
    @TableField(value = "role_name")
    private String roleName;
    @TableField(value = "table_name")
    private String tableName;
    @TableField(value = "can_insert")
    private Boolean canInsert;
    @TableField(value = "can_delete")
    private Boolean canDelete;
    @TableField(value = "can_update")
    private Boolean canUpdate;
    @TableField(value = "can_select")
    private Boolean canSelect;
}