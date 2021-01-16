package org.wyyt.admin.ui.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;
import org.wyyt.admin.ui.common.Constants;
import org.wyyt.admin.ui.entity.vo.AdminVo;

import javax.servlet.http.HttpServletRequest;

/**
 * The argument resolver for login
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public class LoginArgumentResolver implements WebArgumentResolver {
    @Override
    public Object resolveArgument(final MethodParameter methodParameter,
                                  final NativeWebRequest nativeWebRequest) {
        final Class<?> parameterType = methodParameter.getParameterType();
        if (null != parameterType) {
            final HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
            if (parameterType.equals(AdminVo.class)) {
                return request.getAttribute(Constants.CURRENT_ADMIN_LOGIN);
            }
        }
        return UNRESOLVED;
    }
}