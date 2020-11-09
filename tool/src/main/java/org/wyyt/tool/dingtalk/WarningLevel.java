package org.wyyt.tool.dingtalk;

import lombok.Getter;

/**
 * the entity for message's warning level
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Getter
public enum WarningLevel {
    NORMAL(0, "正常"),
    MINOR(1, "轻微"),
    WARNING(2, "警告"),
    CRITICAL(3, "严重"),
    FATAL(4, "致命");

    private final int code;
    private final String description;

    WarningLevel(final int code,
                 final String description) {
        this.code = code;
        this.description = description;
    }

    public static WarningLevel get(final Integer code) {
        for (final WarningLevel item : WarningLevel.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }

    public static WarningLevel get(final String description) {
        for (final WarningLevel item : WarningLevel.values()) {
            if (item.getDescription().equals(description)) {
                return item;
            }
        }
        return null;
    }
}