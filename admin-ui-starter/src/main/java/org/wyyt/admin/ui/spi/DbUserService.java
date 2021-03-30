package org.wyyt.admin.ui.spi;

import org.springframework.util.ObjectUtils;
import org.wyyt.admin.ui.common.Utils;
import org.wyyt.admin.ui.entity.dto.SysAdmin;
import org.wyyt.admin.ui.entity.vo.AdminVo;
import org.wyyt.admin.ui.service.SysAdminService;
import org.wyyt.ldap.entity.UserInfo;
import org.wyyt.tool.anno.TranSave;

import java.util.ArrayList;
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
public class DbUserService implements UserService {
    private final SysAdminService sysAdminService;

    public DbUserService(final SysAdminService sysAdminService) {
        this.sysAdminService = sysAdminService;
    }

    @Override
    public boolean authenticate(final String username,
                                final String password) throws Exception {
        if (ObjectUtils.isEmpty(username) || ObjectUtils.isEmpty(password)) {
            return false;
        }
        final SysAdmin sysAdmin = this.sysAdminService.getByUsername(username);
        if (null == sysAdmin) {
            return false;
        }
        return Utils.hash(password).equals(sysAdmin.getPassword());
    }

    @Override
    @TranSave
    public AdminVo getByUserName(final String username) throws Exception {
        final SysAdmin sysAdmin = this.sysAdminService.getByUsername(username);
        return this.sysAdminService.getByUsernameAndPassword(sysAdmin.getUsername(), sysAdmin.getPassword());
    }

    @Override
    public List<UserInfo> search(final String keyword,
                                 final int offset,
                                 final int limit) throws Exception {
        final List<SysAdmin> sysAdminList = this.sysAdminService.search(keyword, offset, limit);
        final List<UserInfo> result = new ArrayList<>(sysAdminList.size());
        for (final SysAdmin sysAdmin : sysAdminList) {
            final UserInfo userInfo = new UserInfo();
            userInfo.setUsername(sysAdmin.getUsername());
            userInfo.setName(sysAdmin.getName());
            userInfo.setPhoneNumber(sysAdmin.getPhoneNumber());
            userInfo.setEmail(sysAdmin.getEmail());
            userInfo.setRemark(sysAdmin.getRemark());
            result.add(userInfo);
        }
        return result;
    }
}
