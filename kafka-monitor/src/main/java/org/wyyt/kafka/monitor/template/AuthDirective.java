package org.wyyt.kafka.monitor.template;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.wyyt.kafka.monitor.entity.vo.AdminVo;
import org.wyyt.kafka.monitor.entity.vo.PageVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * the abstract class of authentication
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public abstract class AuthDirective {

    @Autowired
    protected HttpServletRequest request;

    protected boolean checkPermission(final Operation operation) {

        if (null == operation) {
            return false;
        }

        final String uri = request.getRequestURI();

        switch (operation) {
            case INSERT:
                return this.checkPermission(uri, PageVo::getCanInsert);
            case DELETE:
                return this.checkPermission(uri, PageVo::getCanDelete);
            case UPDATE:
                return this.checkPermission(uri, PageVo::getCanUpdate);
            case SELECT:
                return this.checkPermission(uri, (permission) -> permission.getCanDelete() || permission.getCanInsert() || permission.getCanUpdate()
                        || permission.getCanSelect());
        }
        return false;
    }

    private boolean checkPermission(String uri,
                                    final HandlePermission handlePermission) {
        final AdminVo adminVo = (AdminVo) SecurityUtils.getSubject().getPrincipal();
        if (null == adminVo) {
            return false;
        } else if (adminVo.getSysRole().getSuperAdmin()) {
            return true;
        } else if (null == adminVo.getPermissions() || adminVo.getPermissions().size() < 1) {
            return false;
        }

        if (uri == null) {
            uri = "";
        }

        final PageVo pageVo = this.getByUri(adminVo.getPermissions(), uri);
        if (null != pageVo) {
            return handlePermission.check(pageVo);
        }

        return false;
    }

    private PageVo getByUri(final List<PageVo> pageVoList,
                            final String uri) {
        for (final PageVo pageVo : pageVoList) {
            if (pageVo.getUrl().equals(uri)) {
                return pageVo;
            } else if (null != pageVo.getChildren() && pageVo.getChildren().size() > 0) {
                return getByUri(pageVo.getChildren(), uri);
            }
        }
        return null;
    }

    protected enum Operation {
        INSERT,
        DELETE,
        UPDATE,
        SELECT,
        ONLY_SELECT
    }

    private interface HandlePermission {
        boolean check(final PageVo pageVo);
    }
}