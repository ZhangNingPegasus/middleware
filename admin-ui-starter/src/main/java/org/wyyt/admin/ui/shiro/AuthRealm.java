package org.wyyt.admin.ui.shiro;

import lombok.SneakyThrows;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.wyyt.admin.ui.common.Utils;
import org.wyyt.admin.ui.entity.vo.AdminVo;
import org.wyyt.admin.ui.service.SysAdminService;
import org.wyyt.admin.ui.service.SysRoleService;

/**
 * The authentication realm for shiro
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public class AuthRealm extends AuthorizingRealm {
    @Autowired
    private SysAdminService sysAdminService;
    @Autowired
    private SysRoleService sysRoleService;

    @SneakyThrows
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken) throws AuthenticationException {
        final UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
        final AdminVo adminVo = this.sysAdminService.getByUsernameAndPassword(usernamePasswordToken.getUsername(),
                Utils.hash(new String(usernamePasswordToken.getPassword())));
        if (null == adminVo) {
            return null;
        }
        adminVo.setSysRole(this.sysRoleService.getById(adminVo.getSysRoleId()));
        return new SimpleAuthenticationInfo(adminVo, usernamePasswordToken.getPassword(), usernamePasswordToken.getUsername());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        return new SimpleAuthorizationInfo();
    }
}
