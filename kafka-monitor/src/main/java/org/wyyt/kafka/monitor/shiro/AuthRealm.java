package org.wyyt.kafka.monitor.shiro;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.wyyt.kafka.monitor.entity.vo.AdminVo;
import org.wyyt.kafka.monitor.service.dto.SysAdminService;
import org.wyyt.kafka.monitor.util.SecurityUtil;

/**
 * The authentication realm for shiro
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public class AuthRealm extends AuthorizingRealm {
    private final SysAdminService sysAdminService;

    public AuthRealm(final SysAdminService sysAdminService) {
        this.sysAdminService = sysAdminService;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken) throws AuthenticationException {
        final UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
        final AdminVo adminVo = this.sysAdminService.getByUsernameAndPassword(usernamePasswordToken.getUsername(), SecurityUtil.hash(new String(usernamePasswordToken.getPassword())));
        if (null == adminVo) {
            return null;
        }
        return new SimpleAuthenticationInfo(adminVo, usernamePasswordToken.getPassword(), usernamePasswordToken.getUsername());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        return new SimpleAuthorizationInfo();
    }
}