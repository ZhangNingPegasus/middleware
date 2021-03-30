package org.wyyt.admin.ui.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.wyyt.ldap.entity.LoginMode;

/**
 * Properties of AdminUI
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
@ConfigurationProperties("admin.ui")
public class AdminUiProperties {
    /**
     * 用户登录模式, 数据库模式:db; ldap模式: ldap
     */
    private LoginMode loginMode = LoginMode.DB;
    ;
    /**
     * 标题
     */
    private String title = "";

    /**
     * 应用全名称
     */
    private String fullName = "";

    /**
     * 应用简称
     */
    private String shortName = "";
}
