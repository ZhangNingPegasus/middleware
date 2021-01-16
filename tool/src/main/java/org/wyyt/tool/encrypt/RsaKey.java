package org.wyyt.tool.encrypt;

import lombok.Data;

/**
 * the entity of RSA key
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class RsaKey {
    private String privateKey;
    private String publicKey;
}
