package dev.solaris.memory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.embeddings")
public class AppEmbeddingsProperties {

    private boolean enabled;
    /** "ollama" (padrão, local) | "gemini" (pedido do usuário em 15/10, embeddings assimétricos document/query). */
    private String provider = "ollama";
    private String ollamaBaseUrl;
    private String model;
    private int dimensions;
    private double maxDistance;
    private double semanticMargin;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getOllamaBaseUrl() {
        return ollamaBaseUrl;
    }

    public void setOllamaBaseUrl(String ollamaBaseUrl) {
        this.ollamaBaseUrl = ollamaBaseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getDimensions() {
        return dimensions;
    }

    public void setDimensions(int dimensions) {
        this.dimensions = dimensions;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public double getSemanticMargin() {
        return semanticMargin;
    }

    public void setSemanticMargin(double semanticMargin) {
        this.semanticMargin = semanticMargin;
    }
}
