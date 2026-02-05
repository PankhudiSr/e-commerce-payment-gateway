package com.intern.ecommerce.paymentgateway.security;

import com.intern.ecommerce.paymentgateway.common.Constants;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class AESUtil {

    // Generate AES key using SHA-1
    private static SecretKeySpec getSecretKeySpec() {
        try {
            MessageDigest sha1 = MessageDigest.getInstance(Constants.HASH_ALGORITHM);
            byte[] hash = sha1.digest(
                    Constants.AES_SECRET_KEY.getBytes(StandardCharsets.UTF_8)
            );

            byte[] aesKey = Arrays.copyOf(hash, 16); // AES-128
            return new SecretKeySpec(aesKey, Constants.AES_ALGORITHM);

        } catch (Exception e) {
            throw new RuntimeException("Error generating AES key", e);
        }
    }

    // Encrypt
    public static String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(Constants.AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec());
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES Encryption failed", e);
        }
    }

    // Decrypt
    public static String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(Constants.AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec());
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES Decryption failed", e);
        }
    }
}
