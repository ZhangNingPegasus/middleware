package org.wyyt.admin.ui.shiro;

import lombok.SneakyThrows;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.wyyt.admin.ui.entity.vo.AdminVo;
import org.wyyt.admin.ui.spi.UserService;
import org.wyyt.tool.anno.TranSave;

/**
 * The authentication realm for shiro
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class AuthRealm extends AuthorizingRealm {
    @Autowired
    private UserService userService;

    @SneakyThrows
    @Override
    @TranSave
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken) throws AuthenticationException {
        final UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
        final String username = usernamePasswordToken.getUsername();
        final String password = new String(usernamePasswordToken.getPassword());
        if (!this.userService.authenticate(username, password)) {
            return null;
        }
        final AdminVo adminVo = this.userService.getByUserName(username);
        return new SimpleAuthenticationInfo(adminVo, usernamePasswordToken.getPassword(), usernamePasswordToken.getUsername());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        return new SimpleAuthorizationInfo();
    }
}
