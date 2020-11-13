package org.wyyt.sql.tool.context;

import org.apache.shiro.SecurityUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.wyyt.sql.tool.common.Constants;
import org.wyyt.sql.tool.entity.vo.AdminVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The interceptor for login
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public final class LoginInterceptor implements AsyncHandlerInterceptor {
    @Override
    public final boolean preHandle(final HttpServletRequest request,
                                   final HttpServletResponse response,
                                   final Object handler) {
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            try {
                final AdminVo adminVo = (AdminVo) SecurityUtils.getSubject().getPrincipal();
                if (null != adminVo) {
                    request.setAttribute(Constants.CURRENT_ADMIN_LOGIN, adminVo);
                }
            } catch (final Exception ignored) {
            }
        }
        return true;
    }
}