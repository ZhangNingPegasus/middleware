package org.wyyt.db2es.admin.shiro;

import lombok.SneakyThrows;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.wyyt.db2es.admin.entity.vo.AdminVo;
import org.wyyt.db2es.admin.service.SysPageService;

/**
 * The credential matcher for shiro
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
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
