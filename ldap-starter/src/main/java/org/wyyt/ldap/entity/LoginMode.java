package org.wyyt.ldap.entity;

import lombok.Getter;

/**
 * the enum of Login Mode
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Getter
public enum LoginMode {
    DB(1, "db"),
    LDAP(2, "ldap");

    private final int code;
    private final String name;

    LoginMode(final int code,
              final String name) {
        this.code = code;
        this.name = name;
    }

    public static LoginMode get(final Integer code) {
        for (final LoginMode item : LoginMode.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }

    public static LoginMode get(final String name) {
        for (final LoginMode item : LoginMode.values()) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }
}