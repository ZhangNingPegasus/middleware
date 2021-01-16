package org.wyyt.sharding.sqltool.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.wyyt.sharding.sqltool.entity.dto.SysRole;

import java.io.Serializable;
import java.util.Date;

/**
 * The view object of administrator
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public final class AdminVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sysRoleId;
    private String roleName;
    private String username;
    private String name;
    private String phoneNumber;
    private String email;
    private String remark;
    private SysRole role;
    private Date createTime;
}