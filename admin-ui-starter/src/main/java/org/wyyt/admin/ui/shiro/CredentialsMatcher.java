package org.wyyt.admin.ui.shiro;

import lombok.SneakyThrows;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.wyyt.admin.ui.entity.vo.AdminVo;
import org.wyyt.admin.ui.service.SysPageService;

/**
 * The credential matcher for shiro
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public class CredentialsMatcher extends SimpleCredentialsMatcher {

    @Autowired
    private SysPageService sysPageService;

    @SneakyThrows
    @Override
    public boolean doCredentialsMatch(final AuthenticationToken token,
                                      final AuthenticationInfo info) {
        final AdminVo appVo = (AdminVo) info.getPrincipals().getPrimaryPrincipal();
        this.sysPageService.fillPages(appVo);
        return true;
    }
}