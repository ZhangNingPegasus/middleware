package org.wyyt.apollo.code;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import lombok.Getter;

/**
 * the enum for Apollo's result
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Getter
public enum Codes {
    SUCCESS(200L, true, ""),
    BAD_REQUEST(400L, false, "客户端传入参数的错误，如操作人不存在，namespace不存在等等，客户端需要根据提示信息检查对应的参数是否正确"),
    UNAUTHORIZED(401L, false, "接口传入的token非法或者已过期，客户端需要检查token是否传入正确"),
    FORBIDDEN(403L, false, "接口要访问的资源未得到授权，比如只授权了对A应用下Namespace的管理权限，但是却尝试管理B应用下的配置"),
    NOT_FOUND(404L, false, "接口要访问的资源不存在，一般是URL或URL的参数错误"),
    METHOD_NOT_ALLOWED(405L, false, "接口访问的Method不正确，比如应该使用POST的接口使用了GET访问等，客户端需要检查接口访问方式是否正确"),
    INTERNAL_SERVER_ERROR(500L, false, "其它类型的错误默认都会返回500，对这类错误如果应用无法根据提示信息找到原因的话，可以找Apollo研发团队一起排查问题");

    private final Long code;
    private final String description;
    private final Boolean success;

    Codes(final long code,
          final Boolean success,
          final String description) {
        this.code = code;
        this.success = success;
        this.description = description;
    }

    public static Codes get(final Long code) {
        if (null == code) {
            return null;
        }

        for (final Codes item : Codes.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static Codes get(final String description) {
        if (StringUtils.isBlank(description)) {
            return null;
        }

        for (final Codes item : Codes.values()) {
            if (item.getDescription().equals(description)) {
                return item;
            }
        }
        return null;
    }
}