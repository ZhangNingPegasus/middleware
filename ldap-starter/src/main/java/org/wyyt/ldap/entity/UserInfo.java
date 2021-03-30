package org.wyyt.ldap.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The entity of user information
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode
@Data
@ToString
public class UserInfo {
    private String username;
    private String password;
    private String name;
    private String phoneNumber;
    private String email;
    private String remark;
}