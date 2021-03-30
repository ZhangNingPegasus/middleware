package org.wyyt.admin.ui.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.wyyt.admin.ui.service.SysAdminService;
import org.wyyt.admin.ui.service.SysRoleService;
import org.wyyt.admin.ui.spi.DbUserService;
import org.wyyt.admin.ui.spi.LdapUserService;
import org.wyyt.admin.ui.spi.UserService;
import org.wyyt.ldap.entity.LoginMode;
import org.wyyt.ldap.service.LdapService;
import org.wyyt.tool.db.CrudService;
import org.wyyt.tool.exception.BusinessException;

import javax.sql.DataSource;

/**
 * Providing the CRUD of database
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@EnableConfigurationProperties({AdminUiProperties.class})
public class AdminUiAutoConfiguration {

    private final AdminUiProperties adminUiProperties;

    public AdminUiAutoConfiguration(final AdminUiProperties adminUiProperties) {
        this.adminUiProperties = adminUiProperties;
    }

    @Bean(name = "adminUiCrudService")
    @Primary
    public CrudService crudService(final DataSource dataSource) {
        return new CrudService(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserService ldapUserService(@Autowired(required = false) final LdapService ldapService,
                                       final SysAdminService sysAdminService) {
        if (this.adminUiProperties.getLoginMode() == LoginMode.DB) {
            return new DbUserService(sysAdminService);
        } else if (this.adminUiProperties.getLoginMode() == LoginMode.LDAP) {
            return new LdapUserService(ldapService, sysAdminService);
        }
        throw new BusinessException("配置项[admin.ui.login-mode]的值暂不支持");
    }

    @Bean
    @ConditionalOnMissingBean
    public SysAdminService sysAdminService(final @Qualifier("adminUiCrudService") CrudService crudService,
                                           final SysRoleService sysRoleService) {
        return new SysAdminService(crudService, sysRoleService);
    }

}