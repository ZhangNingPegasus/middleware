package org.wyyt.admin.ui.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.admin.ui.entity.base.BaseDto;

/**
 * The entity for table `sys_permission`. Using for managing role's permission.
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
@TableName(value = "`sys_permission`")
public final class SysPermission extends BaseDto {
    private static final long serialVersionUID = 1L;

    @TableField(value = "`sys_role_id`")
    private Long sysRoleId;

    @TableField(value = "`sys_page_id`")
    private Long sysPageId;

    @TableField(value = "`can_insert`")
    private Boolean canInsert;

    @TableField(value = "`can_delete`")
    private Boolean canDelete;

    @TableField(value = "`can_update`")
    private Boolean canUpdate;

    @TableField(value = "`can_select`")
    private Boolean canSelect;
}