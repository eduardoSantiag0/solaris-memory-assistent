package dev.solaris.memory.web;

import dev.solaris.memory.dto.ChatQueryRequest;
import dev.solaris.memory.dto.ChatQueryResponse;
import dev.solaris.memory.dto.CreateMemoryRequest;
import dev.solaris.memory.dto.MemoryResponse;
import dev.solaris.memory.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Exercises the real Ollama instance on this machine (M3) rather than
 * mocking the embedding client — skips itself when Ollama isn't reachable
 * so the suite stays portable to machines/CI without it.
 */
class SemanticSearchIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void requireOllama() {
        boolean reachable;
        try {
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder(URI.create("http://localhost:11434/api/version"))
                    .timeout(Duration.ofSeconds(2)).GET().build();
            reachable = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
        } catch (Exception e) {
            reachable = false;
        }
        assumeTrue(reachable, "Ollama não está acessível em localhost:11434 — pulando testes de busca semântica.");
    }

    // A paraphrase sharing no keywords with the stored text — keyword search
    // (RF-05's priority path) must find nothing, so this only passes through
    // the semantic fallback (RF-13: must come back labeled as such).
    @Test
    void paraphrasedQuestionFindsMemoryByMeaningNotKeywords() throws InterruptedException {
        createMemory("Deixei o guarda-chuva emprestado na portaria do prédio.");

        ChatQueryResponse response = pollUntilFound("Onde está o item que esqueci na entrada do edifício?");

        assertThat(response.found()).isTrue();
        assertThat(response.answer()).contains("guarda-chuva");
        assertThat(response.warnings()).anyMatch(w -> w.toLowerCase().contains("semântica") || w.toLowerCase().contains("semantica"));
    }

    // CT-13 (Correções 14/10 item 1, RF-04/CT-05): a question with no real
    // relation to anything stored must never surface the "nearest anyway"
    // memory — the minimum relevance threshold on the vector search has to
    // discard it. Waits for the embedding to land via a related paraphrase
    // first, so the unrelated question below isn't just hitting an empty index.
    @Test
    void unrelatedQuestionBelowRelevanceThresholdReturnsNotFound() throws InterruptedException {
        createMemory("Meu RG é 12.345.678-9.");
        pollUntilFound("Qual é o número do meu documento de identidade?");

        ChatQueryResponse answer = query("Qual a cor do céu?");

        assertThat(answer.found()).isFalse();
        assertThat(answer.sources()).isEmpty();
    }

    private ChatQueryResponse query(String question) {
        ChatQueryRequest request = new ChatQueryRequest(question, UUID.randomUUID().toString());
        ResponseEntity<ChatQueryResponse> response = restTemplate.exchange(
                "/api/v1/chat/query", HttpMethod.POST, authEntity(restTemplate, request), ChatQueryResponse.class);
        return response.getBody();
    }

    private MemoryResponse createMemory(String content) {
        CreateMemoryRequest request = new CreateMemoryRequest(content, null, null, null, UUID.randomUUID().toString());
        ResponseEntity<MemoryResponse> response = restTemplate.exchange(
                "/api/v1/memories", HttpMethod.POST, authEntity(restTemplate, request), MemoryResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    /** Embedding generation happens on a background thread (MemoryService.scheduleEmbedding) — give it a few seconds to land. */
    private ChatQueryResponse pollUntilFound(String question) throws InterruptedException {
        ChatQueryResponse last = null;
        for (int attempt = 0; attempt < 20; attempt++) {
            ChatQueryRequest request = new ChatQueryRequest(question, UUID.randomUUID().toString());
            ResponseEntity<ChatQueryResponse> response = restTemplate.exchange(
                    "/api/v1/chat/query", HttpMethod.POST, authEntity(restTemplate, request), ChatQueryResponse.class);
            last = response.getBody();
            if (last != null && last.found()) {
                return last;
            }
            Thread.sleep(500);
        }
        return last;
    }
}
