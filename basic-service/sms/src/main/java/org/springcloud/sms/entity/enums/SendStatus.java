package org.springcloud.sms.entity.enums;

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
public enum SendStatus {
    PENDING(0, "发送中"),
    SUCCESS(1, "发送成功"),
    ERROR(2, "发送失败");

    private final Integer code;
    private final String description;

    SendStatus(final int code,
               final String description) {
        this.code = code;
        this.description = description;
    }

    public static SendStatus get(final Long code) {
        if (null == code) {
            return null;
        }

        for (final SendStatus item : SendStatus.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
