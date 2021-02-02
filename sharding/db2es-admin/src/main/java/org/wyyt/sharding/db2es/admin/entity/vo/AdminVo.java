package org.wyyt.sharding.db2es.admin.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.sharding.db2es.admin.entity.dto.SysRole;
import org.wyyt.sharding.db2es.core.entity.persistent.BaseDto;

import java.util.List;

/**
 * The View Object for table `sys_admin`. Using for saving information of administrator.
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
public final class AdminVo extends BaseDto {
    private static final long serialVersionUID = 1L;

    @TableField(value = "`sys_role_id`")
    private Long sysRoleId;

    @TableField(value = "`role_name`")
    private String roleName;

    @TableField(value = "`username`")
    private String username;

    @TableField(value = "`name`")
    private String name;

    @TableField(value = "`phone_number`")
    private String phoneNumber;

    @TableField(value = "`email`")
    private String email;

    @TableField(value = "`remark`")
    private String remark;

    @TableField(exist = false)
    private String defaultPage;

    @TableField(exist = false)
    private SysRole sysRole;

    @TableField(exist = false)
    private List<PageVo> permissions;
}