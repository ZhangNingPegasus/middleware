package org.wyyt.sql.tool.shiro;

import lombok.SneakyThrows;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.wyyt.sql.tool.database.Db;
import org.wyyt.sql.tool.entity.dto.SysAdmin;
import org.wyyt.sql.tool.entity.vo.AdminVo;

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
public final class AuthRealm extends AuthorizingRealm {
    @Autowired
    private Db db;

    @SneakyThrows
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken) throws AuthenticationException {
        final UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
        final SysAdmin sysAdmin = this.db.getAdminByUsernameAndPassword(usernamePasswordToken.getUsername(), new String(usernamePasswordToken.getPassword()));
        if (null == sysAdmin) {
            return null;
        }
        final AdminVo adminVo = new AdminVo();
        BeanUtils.copyProperties(sysAdmin, adminVo);
        adminVo.setRole(db.getRoleById(adminVo.getSysRoleId()));
        return new SimpleAuthenticationInfo(adminVo, usernamePasswordToken.getPassword(), usernamePasswordToken.getUsername());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        return new SimpleAuthorizationInfo();
    }
}