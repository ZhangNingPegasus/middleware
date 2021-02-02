package org.wyyt.tool.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * the common functions of RSA encrypt
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class RsaTool {
    private static final String ALGORITHM_RSA = "RSA";

    public static RsaKey generatorKeyPair() throws Exception {
        final KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        keyPairGen.initialize(1024);
        final KeyPair keyPair = keyPairGen.generateKeyPair();
        final RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        byte[] keyBs = rsaPublicKey.getEncoded();
        String publicKey = encodeBase64(keyBs);
        final RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        keyBs = rsaPrivateKey.getEncoded();
        String privateKey = encodeBase64(keyBs);
        final RsaKey result = new RsaKey();
        result.setPrivateKey(privateKey);
        result.setPublicKey(publicKey);
        return result;
    }

    public static String decrypt(final String value,
                                 final PrivateKey privateKey) throws Exception {
        final Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        cipher.update(RsaTool.decodeBase64(value));
        return new String(cipher.doFinal(), StandardCharsets.UTF_8);
    }

    public static String encrypt(final String value,
                                 final PublicKey publicKey) throws Exception {
        final Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        cipher.update(value.getBytes(StandardCharsets.UTF_8));
        return RsaTool.encodeBase64(cipher.doFinal());
    }

    public static String encrypt(final String value,
                                 final String strPublicKey) throws Exception {
        return RsaTool.encrypt(value, RsaTool.getPublicKey(strPublicKey));
    }

    public static String decrypt(final String value,
                                 final String strPrivateKey) throws Exception {
        return RsaTool.decrypt(value, RsaTool.getPrivateKey(strPrivateKey));
    }

    private static PublicKey getPublicKey(final String publicKey) throws Exception {
        final X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(RsaTool.decodeBase64(publicKey));
        final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
        return keyFactory.generatePublic(publicKeySpec);
    }

    private static PrivateKey getPrivateKey(final String privateKey) throws Exception {
        final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(decodeBase64(privateKey));
        final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
        return keyFactory.generatePrivate(privateKeySpec);
    }

    private static String encodeBase64(final byte[] source) {
        return new String(Base64.encodeBase64(source), StandardCharsets.UTF_8);
    }

    private static byte[] decodeBase64(final String source) {
        return Base64.decodeBase64(source.getBytes(StandardCharsets.UTF_8));
    }
}