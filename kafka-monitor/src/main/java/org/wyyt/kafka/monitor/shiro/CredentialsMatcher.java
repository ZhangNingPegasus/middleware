package org.wyyt.kafka.monitor.shiro;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.wyyt.kafka.monitor.entity.vo.AdminVo;
import org.wyyt.kafka.monitor.service.dto.SysPageService;

/**
 * The credential matcher for shiro
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public class CredentialsMatcher extends SimpleCredentialsMatcher {
    private final SysPageService sysPageService;

    public CredentialsMatcher(final SysPageService sysPageService) {
        this.sysPageService = sysPageService;
    }

    @Override
    public boolean doCredentialsMatch(final AuthenticationToken token, final AuthenticationInfo info) {
        final AdminVo appVo = (AdminVo) info.getPrincipals().getPrimaryPrincipal();
        sysPageService.fillPages(appVo);
        return true;
    }
}