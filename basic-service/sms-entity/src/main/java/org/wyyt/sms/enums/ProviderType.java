package org.wyyt.sms.enums;

import lombok.Getter;

/**
 * The enum of send status
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Getter
public enum ProviderType {
    /**
     * 阿里云短信
     */
    AliYun(1, "阿里云短信"),
    /**
     * 梦网短信
     */
    MONTNETS(2, "梦网短信");

    private final Integer code;
    private final String description;

    ProviderType(final int code,
                 final String description) {
        this.code = code;
        this.description = description;
    }

    public static ProviderType get(final Integer code) {
        if (null == code) {
            return null;
        }

        for (final ProviderType item : ProviderType.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
