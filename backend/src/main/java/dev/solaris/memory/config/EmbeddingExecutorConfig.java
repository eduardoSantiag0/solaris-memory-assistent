package dev.solaris.memory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class EmbeddingExecutorConfig {

    /**
     * Embedding calls to Ollama run here, off the request thread, so a slow
     * or unreachable Ollama never adds latency to the write path (RNF-07
     * targets ~2s for a write; a single-user app doesn't need more than one
     * worker for best-effort background enrichment).
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService embeddingExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "embedding-worker");
            t.setDaemon(true);
            return t;
        });
    }
}
