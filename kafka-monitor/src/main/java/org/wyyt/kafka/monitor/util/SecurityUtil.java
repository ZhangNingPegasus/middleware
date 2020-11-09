package org.wyyt.kafka.monitor.util;

import org.apache.shiro.crypto.hash.Md5Hash;

/**
 * The tool for security
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public class SecurityUtil {
    private final static String SALT = "PEgASuS";

    public static String hash(String value) {
        return new Md5Hash(value, SALT).toString();
    }
}