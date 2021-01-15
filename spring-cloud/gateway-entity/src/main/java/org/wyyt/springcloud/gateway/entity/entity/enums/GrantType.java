package org.wyyt.springcloud.gateway.entity.entity.enums;

import lombok.Getter;

/**
 * The enum of grant type
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021        Initialize  *
 * *****************************************************************
 */
@Getter
public enum GrantType {
    AUTHORIZATION_CODE("authorization_code", "authorization_code"),
    IMPLICIT("implicit", "implicit"),
    PASSWORD("password", "password"),
    CLIENT_CREDENTIALS("client_credentials", "client_credentials"),
    REFRESH_TOKEN("refresh_token", "refresh_token");

    private final String code;
    private final String description;

    GrantType(final String code,
              final String description) {
        this.code = code;
        this.description = description;
    }

    public static GrantType get(final String code) {
        for (final GrantType item : GrantType.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
