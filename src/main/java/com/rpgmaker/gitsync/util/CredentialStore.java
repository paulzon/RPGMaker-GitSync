package com.rpgmaker.gitsync.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Very small helper f�r simple Token-Verschl�sselung (kein Ersatz f�r Keychain).
 */
public final class CredentialStore {

    private static final Logger LOGGER = Logger.getLogger(CredentialStore.class.getName());
    private static final String PREFIX = "enc:";

    private CredentialStore() {
    }

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, buildKey());
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return PREFIX + Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException ex) {
            LOGGER.log(Level.WARNING, "Konnte Token nicht verschl�sseln.", ex);
            return plainText;
        }
    }

    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) {
            return "";
        }
        if (!encryptedText.startsWith(PREFIX)) {
            return encryptedText;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, buildKey());
            byte[] decoded = Base64.getDecoder().decode(encryptedText.substring(PREFIX.length()));
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            LOGGER.log(Level.WARNING, "Konnte Token nicht entschl�sseln.", ex);
            return "";
        }
    }

    private static SecretKeySpec buildKey() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String secretSeed = System.getProperty("user.name", "user") + "-" + System.getProperty("os.name", "os");
        byte[] hashed = digest.digest(secretSeed.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(Arrays.copyOf(hashed, 16), "AES");
    }
}
