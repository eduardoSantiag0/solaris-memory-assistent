package dev.solaris.memory.web;

import dev.solaris.memory.dto.CreateEventRequest;
import dev.solaris.memory.dto.CreateMemoryRequest;
import dev.solaris.memory.dto.EventDetailResponse;
import dev.solaris.memory.dto.EventResponse;
import dev.solaris.memory.dto.MemoryResponse;
import dev.solaris.memory.dto.UpdateEventRequest;
import dev.solaris.memory.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createListGetUpdateAndDeleteAnEvent() {
        EventResponse created = createEvent("Viagem a Floripa", "Férias de janeiro");
        assertThat(created.memoryCount()).isEqualTo(0);

        List<EventResponse> listed = list();
        assertThat(listed).extracting(EventResponse::id).contains(created.id());

        EventDetailResponse detail = get(created.id());
        assertThat(detail.title()).isEqualTo("Viagem a Floripa");
        assertThat(detail.memories()).isEmpty();

        UpdateEventRequest update = new UpdateEventRequest("Viagem a Floripa 2026", null, false);
        ResponseEntity<EventResponse> updated = restTemplate.exchange(
                "/api/v1/events/" + created.id(), HttpMethod.PATCH, authEntity(restTemplate, update), EventResponse.class);
        assertThat(updated.getBody().title()).isEqualTo("Viagem a Floripa 2026");

        ResponseEntity<Void> delete = restTemplate.exchange(
                "/api/v1/events/" + created.id(), HttpMethod.DELETE, authEntity(restTemplate), Void.class);
        assertThat(delete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> afterDelete = restTemplate.exchange(
                "/api/v1/events/" + created.id(), HttpMethod.GET, authEntity(restTemplate), String.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addingAndRemovingMemoriesUpdatesTheEventWithoutTouchingTheMemories() {
        MemoryResponse memoryA = createMemory("Cheguei em Floripa", Instant.parse("2026-01-10T10:00:00Z"));
        MemoryResponse memoryB = createMemory("Fui à praia da Joaquina", Instant.parse("2026-01-12T10:00:00Z"));
        EventResponse event = createEvent("Viagem a Floripa", null);

        addMemory(event.id(), memoryA.id());
        addMemory(event.id(), memoryB.id());

        EventDetailResponse detail = get(event.id());
        assertThat(detail.memories()).extracting(MemoryResponse::id).containsExactly(memoryA.id(), memoryB.id());

        ResponseEntity<Void> remove = restTemplate.exchange(
                "/api/v1/events/" + event.id() + "/memories/" + memoryA.id(), HttpMethod.DELETE, authEntity(restTemplate), Void.class);
        assertThat(remove.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        EventDetailResponse afterRemoval = get(event.id());
        assertThat(afterRemoval.memories()).extracting(MemoryResponse::id).containsExactly(memoryB.id());

        // Removing from the event must never delete/modify the memory itself.
        ResponseEntity<MemoryResponse> memoryStillThere = restTemplate.exchange(
                "/api/v1/memories/" + memoryA.id(), HttpMethod.GET, authEntity(restTemplate), MemoryResponse.class);
        assertThat(memoryStillThere.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(memoryStillThere.getBody().content()).isEqualTo("Cheguei em Floripa");
    }

    @Test
    void chronologicalOrderUsesEventAtAndFallsBackToCreatedAt() {
        // No eventAt on either — falls back to createdAt, in creation order.
        MemoryResponse first = createMemory("Primeira memória sem data de evento", null);
        MemoryResponse second = createMemory("Segunda memória sem data de evento", null);
        // An eventAt set in the past takes priority over createdAt ordering.
        MemoryResponse thirdWithEarlyEventAt = createMemory("Memória com data de evento anterior", Instant.parse("2025-06-01T00:00:00Z"));

        EventResponse event = createEvent("Linha do tempo", null);
        addMemory(event.id(), first.id());
        addMemory(event.id(), second.id());
        addMemory(event.id(), thirdWithEarlyEventAt.id());

        EventDetailResponse detail = get(event.id());
        assertThat(detail.memories()).extracting(MemoryResponse::id)
                .containsExactly(thirdWithEarlyEventAt.id(), first.id(), second.id());
    }

    @Test
    void deletingAnEventLeavesItsMemoriesVisibleInTheMemoriesListing() {
        MemoryResponse memory = createMemory("Memória de um evento que será excluído", null);
        EventResponse event = createEvent("Evento temporário", null);
        addMemory(event.id(), memory.id());

        restTemplate.exchange("/api/v1/events/" + event.id(), HttpMethod.DELETE, authEntity(restTemplate), Void.class);

        ResponseEntity<MemoryResponse> stillThere = restTemplate.exchange(
                "/api/v1/memories/" + memory.id(), HttpMethod.GET, authEntity(restTemplate), MemoryResponse.class);
        assertThat(stillThere.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void aMemoryCanBelongToMultipleEvents() {
        MemoryResponse memory = createMemory("Memória compartilhada por dois eventos", null);
        EventResponse eventA = createEvent("Evento A", null);
        EventResponse eventB = createEvent("Evento B", null);

        addMemory(eventA.id(), memory.id());
        addMemory(eventB.id(), memory.id());

        ResponseEntity<EventResponse[]> response = restTemplate.exchange(
                "/api/v1/memories/" + memory.id() + "/events", HttpMethod.GET, authEntity(restTemplate), EventResponse[].class);
        assertThat(response.getBody()).extracting(EventResponse::id)
                .containsExactlyInAnyOrder(eventA.id(), eventB.id());
    }

    private EventResponse createEvent(String title, String description) {
        CreateEventRequest request = new CreateEventRequest(title, description);
        ResponseEntity<EventResponse> response = restTemplate.exchange(
                "/api/v1/events", HttpMethod.POST, authEntity(restTemplate, request), EventResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private List<EventResponse> list() {
        ResponseEntity<EventResponse[]> response = restTemplate.exchange(
                "/api/v1/events", HttpMethod.GET, authEntity(restTemplate), EventResponse[].class);
        return List.of(response.getBody());
    }

    private EventDetailResponse get(java.util.UUID id) {
        ResponseEntity<EventDetailResponse> response = restTemplate.exchange(
                "/api/v1/events/" + id, HttpMethod.GET, authEntity(restTemplate), EventDetailResponse.class);
        return response.getBody();
    }

    private void addMemory(java.util.UUID eventId, java.util.UUID memoryId) {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/events/" + eventId + "/memories/" + memoryId, HttpMethod.POST, authEntity(restTemplate), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private MemoryResponse createMemory(String content, Instant eventAt) {
        CreateMemoryRequest request = new CreateMemoryRequest(content, null, eventAt, null, null);
        ResponseEntity<MemoryResponse> response = restTemplate.exchange(
                "/api/v1/memories", HttpMethod.POST, authEntity(restTemplate, request), MemoryResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }
}
