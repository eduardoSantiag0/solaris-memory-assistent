package dev.solaris.memory.web;

import dev.solaris.memory.dto.AttachmentResponse;
import dev.solaris.memory.dto.MemoryResponse;
import dev.solaris.memory.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

class AttachmentApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final byte[] FAKE_IMAGE_BYTES = {1, 2, 3, 4, 5, 6, 7, 8};

    @Test
    void createsMemoryWithAttachmentAndServesItBackByteForByte() {
        MemoryResponse created = createMemoryWithAttachment(
                "Recibo da consulta médica", "foto.png", "image/png", FAKE_IMAGE_BYTES, "Recibo escaneado");

        assertThat(created.hasAttachment()).isTrue();

        ResponseEntity<AttachmentResponse> metadata = restTemplate.exchange(
                "/api/v1/memories/" + created.id() + "/attachment", HttpMethod.GET, authEntity(restTemplate), AttachmentResponse.class);
        assertThat(metadata.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(metadata.getBody().description()).isEqualTo("Recibo escaneado");
        assertThat(metadata.getBody().filename()).isEqualTo("foto.png");

        ResponseEntity<byte[]> file = restTemplate.exchange(
                "/api/v1/memories/" + created.id() + "/attachment/file", HttpMethod.GET, authEntity(restTemplate), byte[].class);
        assertThat(file.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(file.getBody()).isEqualTo(FAKE_IMAGE_BYTES);
    }

    @Test
    void eventAtSentAsAMultipartFieldIsParsedTheSameAsOnTheJsonEndpoint() {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("content", "Memória com data de evento via multipart");
        parts.add("eventAt", "2026-02-10T08:00:00Z");
        parts.add("attachmentDescription", "Anexo com data de evento");
        parts.add("file", fileResource("foto.png", FAKE_IMAGE_BYTES));

        ResponseEntity<MemoryResponse> response = postMultipart(parts, MemoryResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().eventAt()).isEqualTo(java.time.Instant.parse("2026-02-10T08:00:00Z"));
    }

    @Test
    void missingDescriptionIsAccepted() {
        // Correção 17/10: a descrição deixou de ser obrigatória — só o
        // conteúdo da memória e o arquivo em si continuam exigidos.
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("content", "Memória sem descrição de anexo");
        parts.add("file", fileResource("foto.png", FAKE_IMAGE_BYTES));

        ResponseEntity<MemoryResponse> response = postMultipart(parts, MemoryResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().hasAttachment()).isTrue();

        ResponseEntity<AttachmentResponse> metadata = restTemplate.exchange(
                "/api/v1/memories/" + response.getBody().id() + "/attachment", HttpMethod.GET, authEntity(restTemplate), AttachmentResponse.class);
        assertThat(metadata.getBody().description()).isNull();
    }

    @Test
    void missingFileIsStillRejected() {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("content", "Memória sem nenhum arquivo");
        parts.add("attachmentDescription", "Descrição sem arquivo nenhum");

        ResponseEntity<String> response = postMultipart(parts, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void disallowedContentTypeIsRejected() {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("content", "Memória com tipo de arquivo inválido");
        parts.add("attachmentDescription", "Um executável qualquer");
        parts.add("file", fileResource("arquivo.exe", FAKE_IMAGE_BYTES, "application/octet-stream"));

        ResponseEntity<String> response = postMultipart(parts, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void replacingTheAttachmentKeepsOnlyTheNewOne() {
        MemoryResponse created = createMemoryWithAttachment(
                "Memória com anexo substituído", "primeiro.png", "image/png", FAKE_IMAGE_BYTES, "Primeira versão");

        byte[] replacement = {9, 9, 9};
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("description", "Segunda versão");
        parts.add("file", fileResource("segundo.pdf", replacement, "application/pdf"));

        ResponseEntity<AttachmentResponse> replaced = restTemplate.exchange(
                "/api/v1/memories/" + created.id() + "/attachment", HttpMethod.POST,
                authEntity(restTemplate, parts, MediaType.MULTIPART_FORM_DATA), AttachmentResponse.class);
        assertThat(replaced.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(replaced.getBody().description()).isEqualTo("Segunda versão");

        ResponseEntity<byte[]> file = restTemplate.exchange(
                "/api/v1/memories/" + created.id() + "/attachment/file", HttpMethod.GET, authEntity(restTemplate), byte[].class);
        assertThat(file.getBody()).isEqualTo(replacement);
    }

    @Test
    void deletingTheAttachmentLeavesTheMemoryIntact() {
        MemoryResponse created = createMemoryWithAttachment(
                "Memória cujo anexo será removido", "foto.png", "image/png", FAKE_IMAGE_BYTES, "Vai ser removido");

        ResponseEntity<Void> delete = restTemplate.exchange(
                "/api/v1/memories/" + created.id() + "/attachment", HttpMethod.DELETE, authEntity(restTemplate), Void.class);
        assertThat(delete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> metadata = restTemplate.exchange(
                "/api/v1/memories/" + created.id() + "/attachment", HttpMethod.GET, authEntity(restTemplate), String.class);
        assertThat(metadata.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<MemoryResponse> memory = restTemplate.exchange(
                "/api/v1/memories/" + created.id(), HttpMethod.GET, authEntity(restTemplate), MemoryResponse.class);
        assertThat(memory.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(memory.getBody().hasAttachment()).isFalse();
    }

    private MemoryResponse createMemoryWithAttachment(String content, String filename, String contentType,
                                                        byte[] bytes, String description) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("content", content);
        parts.add("attachmentDescription", description);
        parts.add("file", fileResource(filename, bytes, contentType));

        ResponseEntity<MemoryResponse> response = postMultipart(parts, MemoryResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private <T> ResponseEntity<T> postMultipart(MultiValueMap<String, Object> parts, Class<T> responseType) {
        return restTemplate.exchange(
                "/api/v1/memories/with-attachment", HttpMethod.POST,
                authEntity(restTemplate, parts, MediaType.MULTIPART_FORM_DATA), responseType);
    }

    private ByteArrayResource fileResource(String filename, byte[] bytes) {
        return fileResource(filename, bytes, "image/png");
    }

    private ByteArrayResource fileResource(String filename, byte[] bytes, String contentType) {
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private <T> HttpEntity<T> authEntity(TestRestTemplate restTemplate, T body, MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login(restTemplate));
        headers.setContentType(contentType);
        return new HttpEntity<>(body, headers);
    }
}
