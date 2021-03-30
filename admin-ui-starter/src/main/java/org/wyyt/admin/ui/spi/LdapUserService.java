package org.wyyt.admin.ui.spi;

import org.wyyt.admin.ui.entity.dto.SysAdmin;
import org.wyyt.admin.ui.entity.vo.AdminVo;
import org.wyyt.admin.ui.service.SysAdminService;
import org.wyyt.ldap.entity.LoginMode;
import org.wyyt.ldap.entity.UserInfo;
import org.wyyt.ldap.service.LdapService;
import org.wyyt.tool.anno.TranSave;

import java.util.List;

/**
 * The LDAP implementation of User service
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class LdapUserService implements UserService {
    private final LdapService ldapService;
    private final SysAdminService sysAdminService;

    public LdapUserService(final LdapService ldapService,
                           final SysAdminService sysAdminService) {
        this.ldapService = ldapService;
        this.sysAdminService = sysAdminService;
    }

    @Override
    public boolean authenticate(final String username,
                                final String password) {
        return this.ldapService.authenticate(username, password);
    }

    @Override
    @TranSave
    public AdminVo getByUserName(final String username) throws Exception {
        final UserInfo userInfo = this.ldapService.getByUserName(username);
        if (null == userInfo) {
            return null;
        }
        SysAdmin sysAdmin = this.sysAdminService.getByUsername(username);
        if (null == sysAdmin) {
            this.sysAdminService.add(LoginMode.LDAP, 2L, username, "", userInfo.getName(), userInfo.getPhoneNumber(), userInfo.getEmail(), userInfo.getRemark());
        } else {
            this.sysAdminService.updateInfo(sysAdmin.getId(), userInfo.getName(), userInfo.getPhoneNumber(), userInfo.getEmail(), userInfo.getRemark());
        }
        sysAdmin = this.sysAdminService.getByUsername(username);
        return this.sysAdminService.getByUsernameAndPassword(username, sysAdmin.getPassword());
    }

    @Override
    public List<UserInfo> search(final String keyword,
                                 final int offset,
                                 final int limit) {
        return this.ldapService.search(keyword, offset, limit);
    }
}
