package dev.solaris.memory.security;

import dev.solaris.memory.config.AppEncryptionProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Field-level AES-256-GCM for data stored in a Postgres instance shared with
 * other people on the same server: disk/volume encryption alone would not
 * stop another account that can run SQL directly against the database, so
 * the sensitive {@code content} field is encrypted here, and only this
 * application ever holds the key (RNF-02, RNF-03, and the "criptografia por
 * campo" decision point in doc section 15).
 * <p>
 * Each call uses a fresh random IV, so encrypting the same plaintext twice
 * yields different ciphertext — this is why the converter that uses this
 * class must never be applied to a column that needs exact-match lookups
 * (e.g. clientRequestId).
 */
@Component
public class AesGcmEncryptor {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public AesGcmEncryptor(AppEncryptionProperties properties) {
        byte[] decoded = Base64.getDecoder().decode(properties.getKey());
        if (decoded.length != 32) {
            throw new IllegalStateException("app.encryption.key deve decodificar para exatamente 32 bytes (AES-256)");
        }
        this.key = new SecretKeySpec(decoded, "AES");
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH_BYTES + ciphertext.length);
            buffer.put(iv).put(ciphertext);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException e) {
            // Never include the plaintext in this message — it would end up in logs (RNF-05).
            throw new IllegalStateException("Falha ao cifrar conteúdo", e);
        }
    }

    public String decrypt(String payload) {
        try {
            byte[] raw = Base64.getDecoder().decode(payload);
            ByteBuffer buffer = ByteBuffer.wrap(raw);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao decifrar conteúdo — chave incorreta ou dado corrompido", e);
        }
    }
}
