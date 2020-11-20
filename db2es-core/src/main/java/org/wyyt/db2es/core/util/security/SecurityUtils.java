package org.wyyt.db2es.core.util.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * the utils functions of security
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public final class SecurityUtils {
    public static String encryptData(final String data,
                                     final String key,
                                     final String iv) throws Exception {
        final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        final int blockSize = cipher.getBlockSize();
        final byte[] dataBytes = data.getBytes();
        int plaintextLength = dataBytes.length;
        if (plaintextLength % blockSize != 0) {
            plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
        }
        final byte[] plaintext = new byte[plaintextLength];
        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
        final SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
        final IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
        final byte[] encrypted = cipher.doFinal(plaintext);
        return new String(Base64.encodeBase64(encrypted));
    }

    public static String createLinkString(final Map<String, Object> params) {
        final List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        final StringBuilder signStr = new StringBuilder();
        for (String key : keys) {
            if (null == params.get(key) || StringUtils.isEmpty(params.get(key).toString())) {
                continue;
            }
            signStr.append(key).append("=").append(params.get(key)).append("&");
        }
        if (signStr.length() > 0) {
            return signStr.deleteCharAt(signStr.length() - 1).toString();
        }
        return "";
    }
}