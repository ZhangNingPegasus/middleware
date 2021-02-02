package org.wyyt.admin.ui.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.wyyt.admin.ui.common.Constants;
import org.wyyt.admin.ui.entity.vo.AdminVo;
import org.wyyt.tool.exception.ExceptionTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The interceptor for login
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class LoginInterceptor implements AsyncHandlerInterceptor {
    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) {
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            try {
                final AdminVo adminVo = (AdminVo) SecurityUtils.getSubject().getPrincipal();
                if (null != adminVo) {
                    request.setAttribute(Constants.CURRENT_ADMIN_LOGIN, adminVo);
                }
            } catch (final Exception e) {
                log.error(ExceptionTool.getRootCauseMessage(e), e);
            }
        }
        return true;
    }
}