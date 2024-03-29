package org.wyyt.admin.ui.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.admin.ui.entity.base.BaseDto;

/**
 * The entity for table sys_admin. Using for saving information of administrator.
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
@TableName(value = "`sys_admin`")
public final class SysAdmin extends BaseDto {
    private static final long serialVersionUID = 1L;

    /**
     * 角色类型(1: DB 2: LDAP)
     */
    @TableField(value = "login_mode")
    private Integer loginMode;

    /**
     * 角色id(sys_role表的主键)
     */
    @TableField(value = "`sys_role_id`")
    private Long sysRoleId;

    /**
     * 管理员的登陆用户名
     */
    @TableField(value = "`username`")
    private String username;

    /**
     * 管理员的登陆密码
     */
    @TableField(value = "`password`")
    private String password;

    /**
     * 管理员姓名
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 手机号码
     */
    @TableField(value = "`phone_number`")
    private String phoneNumber;

    /**
     * 邮件地址
     */
    @TableField(value = "`email`")
    private String email;

    /**
     * 备注
     */
    @TableField(value = "`remark`")
    private String remark;
}