package dev.solaris.memory.security;

import dev.solaris.memory.config.AppEncryptionProperties;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesGcmEncryptorTest {

    private final AesGcmEncryptor encryptor = newEncryptor();

    private static AesGcmEncryptor newEncryptor() {
        AppEncryptionProperties properties = new AppEncryptionProperties();
        properties.setKey(Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".substring(0, 32).getBytes()));
        return new AesGcmEncryptor(properties);
    }

    @Test
    void decryptsExactlyWhatWasEncrypted() {
        String plaintext = "Minha chave fica na gaveta azul.";
        String ciphertext = encryptor.encrypt(plaintext);

        assertThat(encryptor.decrypt(ciphertext)).isEqualTo(plaintext);
    }

    @Test
    void sameInputProducesDifferentCiphertextEachTime() {
        String plaintext = "RG 12.345.678-9";

        String first = encryptor.encrypt(plaintext);
        String second = encryptor.encrypt(plaintext);

        assertThat(first).isNotEqualTo(second);
        assertThat(encryptor.decrypt(first)).isEqualTo(plaintext);
        assertThat(encryptor.decrypt(second)).isEqualTo(plaintext);
    }

    @Test
    void ciphertextNeverContainsThePlaintext() {
        String plaintext = "segredo-muito-especifico-123";
        String ciphertext = encryptor.encrypt(plaintext);

        assertThat(ciphertext).doesNotContain(plaintext);
    }

    @Test
    void rejectsKeysThatAreNot32Bytes() {
        AppEncryptionProperties properties = new AppEncryptionProperties();
        properties.setKey(Base64.getEncoder().encodeToString("too-short".getBytes()));

        assertThatThrownBy(() -> new AesGcmEncryptor(properties))
                .isInstanceOf(IllegalStateException.class);
    }
}
