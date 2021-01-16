package org.wyyt.admin.ui.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.admin.ui.entity.base.BaseDto;

/**
 * The View Object for table `sys_permission`.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
public final class PermissionVo extends BaseDto {
    private static final long serialVersionUID = 1L;

    @TableField("`page_id`")
    private Long pageId;

    @TableField("`page_name`")
    private String pageName;

    @TableField("`role_id`")
    private Long roleId;

    @TableField("`role_name`")
    private String roleName;

    @TableField("`can_insert`")
    private Boolean canInsert;

    @TableField("`can_delete`")
    private Boolean canDelete;

    @TableField("`can_update`")
    private Boolean canUpdate;

    @TableField("`can_select`")
    private Boolean canSelect;
}