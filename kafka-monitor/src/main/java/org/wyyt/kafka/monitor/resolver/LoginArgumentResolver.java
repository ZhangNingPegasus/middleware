package org.wyyt.kafka.monitor.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.entity.vo.AdminVo;

import javax.servlet.http.HttpServletRequest;

/**
 * The argument resolver for login.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public class LoginArgumentResolver implements WebArgumentResolver {
    @Override
    public Object resolveArgument(final MethodParameter methodParameter, final NativeWebRequest nativeWebRequest) {
        Class<?> parameterType = methodParameter.getParameterType();
        if (null != parameterType) {
            final HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
            if (parameterType.equals(AdminVo.class)) {
                return request.getAttribute(Constants.CURRENT_ADMIN_LOGIN);
            }
        }
        return UNRESOLVED;
    }
}