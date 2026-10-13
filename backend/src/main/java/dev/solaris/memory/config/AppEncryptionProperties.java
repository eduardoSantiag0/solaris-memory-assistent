package dev.solaris.memory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Base64-encoded 256-bit key used to encrypt memory content at the field level (RNF-03). */
@ConfigurationProperties(prefix = "app.encryption")
public class AppEncryptionProperties {

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
