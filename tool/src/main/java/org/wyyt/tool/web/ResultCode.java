package org.wyyt.tool.web;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * the enum for microservice's result
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Getter
public enum ResultCode {
    SUCCESS(0L, true, "SUCCESS"),
    ERROR(1L, false, "ERROR");

    private final Long code;
    private final String description;
    private final Boolean success;

    ResultCode(final long code,
               final Boolean success,
               final String description) {
        this.code = code;
        this.success = success;
        this.description = description;
    }

    public static ResultCode get(final Long code) {
        if (null == code) {
            return null;
        }

        for (final ResultCode item : ResultCode.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static ResultCode get(final String description) {
        if (StrUtil.isBlank(description)) {
            return null;
        }

        for (final ResultCode item : ResultCode.values()) {
            if (item.getDescription().equals(description)) {
                return item;
            }
        }
        return null;
    }
}