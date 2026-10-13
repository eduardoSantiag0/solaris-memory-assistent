package dev.solaris.memory.domain.converter;

import dev.solaris.memory.security.AesGcmEncryptor;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

/**
 * Transparently encrypts/decrypts a column with AES-256-GCM. Apply only to
 * free-text content that never needs exact-match SQL lookups — ciphertext
 * differs on every write even for identical plaintext, which breaks
 * equality/LIKE queries at the database level by design (that's the whole
 * point: another account with raw DB access sees only ciphertext). Query by
 * fetching candidate rows on non-encrypted columns and filtering decrypted
 * values in the application layer instead (see ChatQueryService, MemoryService).
 */
@Component
@Converter(autoApply = false)
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final AesGcmEncryptor encryptor;

    public EncryptedStringConverter(AesGcmEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : encryptor.decrypt(dbData);
    }
}
