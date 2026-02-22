package com.example.idp.util;

import com.example.idp.config.IdpProperties;
import jakarta.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class AesGcmCrypto {
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_SIZE = 12;

    private final IdpProperties properties;
    private final SecretResolver secretResolver;
    private SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmCrypto(IdpProperties properties, SecretResolver secretResolver) {
        this.properties = properties;
        this.secretResolver = secretResolver;
    }

    @PostConstruct
    public void init() {
        try {
            String encryptionSecret = secretResolver.resolve(properties.getKey().getEncryptionSecret(), "IDP_KEY_ENCRYPTION_SECRET");
            if (encryptionSecret == null || encryptionSecret.length() < 16) {
                throw new IllegalStateException("IDP_KEY_ENCRYPTION_SECRET must be set and at least 16 chars");
            }
            byte[] key = MessageDigest.getInstance("SHA-256").digest(encryptionSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            this.keySpec = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to initialize crypto", e);
        }
    }

    public byte[] encrypt(byte[] plaintext) {
        try {
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);
            return ByteBuffer.allocate(iv.length + ciphertext.length).put(iv).put(ciphertext).array();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to encrypt data", e);
        }
    }

    public byte[] decrypt(byte[] encrypted) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(encrypted);
            byte[] iv = new byte[IV_SIZE];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return cipher.doFinal(ciphertext);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to decrypt data", e);
        }
    }
}
