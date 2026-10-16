package dev.solaris.memory.embedding;

/**
 * Shared contract so {@link EmbeddingService} doesn't care which provider is
 * configured (app.embeddings.provider: "ollama" | "gemini"). Both
 * implementations return null on any soft failure (disabled, unreachable,
 * unconfigured, timeout) — callers must treat that as "skip, don't fail the
 * write/query" (RF-10), never throw.
 */
public interface EmbeddingClient {

    /**
     * DOCUMENT is for text being stored (embedAndStore); QUERY is for the
     * user's question at search time. Providers that support asymmetric,
     * task-aware embeddings (Gemini) use this to get noticeably better
     * retrieval quality than a single symmetric embedding — e.g. "qual o
     * endereço da minha residência" matching a memory that says
     * "apartamento" (pedido do usuário em 15/10). Providers without that
     * concept (Ollama/nomic-embed-text) just ignore it.
     */
    enum TaskType { DOCUMENT, QUERY }

    float[] embed(String text, TaskType taskType);
}
