package org.wyyt.db2es.admin.template;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.wyyt.db2es.admin.entity.vo.AdminVo;
import org.wyyt.db2es.admin.entity.vo.PageVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public abstract class AuthDirective {

    @Autowired
    protected HttpServletRequest request;

    protected boolean checkPermission(final Operation operation) {
        if (null == operation) {
            return false;
        }
        String uri = request.getRequestURI();
        switch (operation) {
            case INSERT:
                return checkPermission(uri, PageVo::getCanInsert);
            case DELETE:
                return checkPermission(uri, PageVo::getCanDelete);
            case UPDATE:
                return checkPermission(uri, PageVo::getCanUpdate);
            case SELECT:
                return checkPermission(uri, (permission) ->
                        permission.getCanDelete() ||
                                permission.getCanInsert() ||
                                permission.getCanUpdate() ||
                                permission.getCanSelect());
        }
        return false;
    }

    private boolean checkPermission(String uri, final HandlePermission handlePermission) {
        final AdminVo adminVo = (AdminVo) SecurityUtils.getSubject().getPrincipal();
        if (null == adminVo) {
            return false;
        } else if (adminVo.getSysRole().getSuperAdmin()) {
            return true;
        } else if (adminVo.getPermissions() == null || adminVo.getPermissions().size() < 1) {
            return false;
        }
        if (null == uri) {
            uri = "";
        }
        final PageVo pageVo = getByUri(adminVo.getPermissions(), uri);
        if (null != pageVo) {
            return handlePermission.check(pageVo);
        }
        return false;
    }

    private PageVo getByUri(final List<PageVo> pageVoList, String uri) {
        for (final PageVo pageVo : pageVoList) {
            if (pageVo.getUrl().equals(uri)) {
                return pageVo;
            } else if (null != pageVo.getChildren() && !pageVo.getChildren().isEmpty()) {
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