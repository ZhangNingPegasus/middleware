package org.wyyt.kafka.monitor.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.kafka.monitor.entity.base.BaseDto;

/**
 * The entity for table sys_role. Using for saving information of role.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`sys_role`")
public class SysRole extends BaseDto {
    /**
     * 角色名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 是否是超级管理员(true:是; false:否)
     */
    @TableField(value = "`super_admin`")
    private Boolean superAdmin;

    /**
     * 角色说明
     */
    @TableField(value = "`remark`")
    private String remark;
}
