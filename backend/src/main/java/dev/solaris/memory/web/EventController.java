package dev.solaris.memory.web;

import dev.solaris.memory.domain.Event;
import dev.solaris.memory.dto.CreateEventRequest;
import dev.solaris.memory.dto.EventDetailResponse;
import dev.solaris.memory.dto.EventResponse;
import dev.solaris.memory.dto.UpdateEventRequest;
import dev.solaris.memory.repository.EventMemoryRepository;
import dev.solaris.memory.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Manual grouping of existing memories into titled, chronological
 * collections. Purely user-driven CRUD — no AI/embedding involvement, and
 * deleting an event never touches the memories it referenced.
 */
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService service;
    private final EventMemoryRepository eventMemoryRepository;

    public EventController(EventService service, EventMemoryRepository eventMemoryRepository) {
        this.service = service;
        this.eventMemoryRepository = eventMemoryRepository;
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        Event event = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(event));
    }

    @GetMapping
    public List<EventResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public EventDetailResponse get(@PathVariable UUID id) {
        return service.getDetail(id);
    }

    @PatchMapping("/{id}")
    public EventResponse update(@PathVariable UUID id, @RequestBody UpdateEventRequest request) {
        return toResponse(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/memories/{memoryId}")
    public ResponseEntity<Void> addMemory(@PathVariable UUID id, @PathVariable UUID memoryId) {
        service.addMemory(id, memoryId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/memories/{memoryId}")
    public ResponseEntity<Void> removeMemory(@PathVariable UUID id, @PathVariable UUID memoryId) {
        service.removeMemory(id, memoryId);
        return ResponseEntity.noContent().build();
    }

    private EventResponse toResponse(Event event) {
        return EventResponse.from(event, eventMemoryRepository.countByEventId(event.getId()));
    }
}
