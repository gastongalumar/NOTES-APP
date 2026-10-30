package com.notasapp.security;

import com.notasapp.config.JwtProperties;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecretCipher {

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public SecretCipher(JwtProperties properties,
                        @Value("${app.security.encryption-key:}") String encryptionKey) {
        String material = encryptionKey == null || encryptionKey.isBlank()
                ? properties.getSecret()
                : encryptionKey;
        byte[] secret = keyMaterial(material);
        try {
            byte[] derived = MessageDigest.getInstance("SHA-256").digest(secret);
            this.key = new SecretKeySpec(derived, "AES");
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo inicializar el cifrado", ex);
        }
    }

    private static byte[] keyMaterial(String value) {
        try {
            byte[] decoded = Decoders.BASE64.decode(value);
            if (decoded.length >= 16) {
                return decoded;
            }
        } catch (RuntimeException ignored) {
            // No es base64: se usa el valor crudo.
        }
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public String encrypt(String plain) {
        try {
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo cifrar el secreto", ex);
        }
    }

    public String decrypt(String encoded) {
        try {
            byte[] all = Base64.getDecoder().decode(encoded);
            byte[] iv = new byte[12];
            byte[] encrypted = new byte[all.length - 12];
            System.arraycopy(all, 0, iv, 0, 12);
            System.arraycopy(all, 12, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo descifrar el secreto", ex);
        }
    }
}
