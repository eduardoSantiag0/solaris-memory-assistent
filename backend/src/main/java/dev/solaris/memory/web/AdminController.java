package dev.solaris.memory.web;

import dev.solaris.memory.embedding.EmbeddingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

/**
 * Manutenção pontual, não parte do fluxo normal do app — protegido pela
 * mesma autenticação de tudo mais (não está na lista de permitAll do
 * SecurityConfig). Hoje só o reembed, chamado manualmente depois de trocar
 * `app.embeddings.provider` (15/10).
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final EmbeddingService embeddingService;
    private final Clock clock;

    public AdminController(EmbeddingService embeddingService, Clock clock) {
        this.embeddingService = embeddingService;
        this.clock = clock;
    }

    @PostMapping("/reembed")
    public ResponseEntity<Map<String, Object>> reembed() {
        int processed = embeddingService.reembedAllActive(Instant.now(clock));
        return ResponseEntity.ok(Map.of("reembedded", processed));
    }
}
