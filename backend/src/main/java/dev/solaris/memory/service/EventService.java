package dev.solaris.memory.service;

import dev.solaris.memory.domain.Event;
import dev.solaris.memory.domain.EventMemory;
import dev.solaris.memory.domain.Memory;
import dev.solaris.memory.dto.CreateEventRequest;
import dev.solaris.memory.dto.EventDetailResponse;
import dev.solaris.memory.dto.EventResponse;
import dev.solaris.memory.dto.MemoryResponse;
import dev.solaris.memory.dto.UpdateEventRequest;
import dev.solaris.memory.exception.EventNotFoundException;
import dev.solaris.memory.repository.AttachmentRepository;
import dev.solaris.memory.repository.EventMemoryRepository;
import dev.solaris.memory.repository.EventRepository;
import dev.solaris.memory.repository.MemoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Manual grouping of existing memories into titled, chronological
 * collections (requirement: "Agrupamento de memórias em eventos"). This
 * service never touches Memory rows beyond reading them — no AI/embedding
 * involvement anywhere here, association is 100% user-driven.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventMemoryRepository eventMemoryRepository;
    private final MemoryRepository memoryRepository;
    private final AttachmentRepository attachmentRepository;
    private final MemoryService memoryService;
    private final Clock clock;

    public EventService(EventRepository eventRepository, EventMemoryRepository eventMemoryRepository,
                         MemoryRepository memoryRepository, AttachmentRepository attachmentRepository,
                         MemoryService memoryService, Clock clock) {
        this.eventRepository = eventRepository;
        this.eventMemoryRepository = eventMemoryRepository;
        this.memoryRepository = memoryRepository;
        this.attachmentRepository = attachmentRepository;
        this.memoryService = memoryService;
        this.clock = clock;
    }

    @Transactional
    public Event create(CreateEventRequest request) {
        Event event = new Event(UUID.randomUUID(), request.title(), blankToNull(request.description()), Instant.now(clock));
        return eventRepository.save(event);
    }

    public Event getVisible(UUID id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    @Transactional
    public Event update(UUID id, UpdateEventRequest request) {
        Event event = getVisible(id);
        event.applyUpdate(request.title(), request.description(), request.clearDescription(), Instant.now(clock));
        return event;
    }

    @Transactional
    public void delete(UUID id) {
        getVisible(id);
        eventRepository.deleteById(id);
    }

    public List<EventResponse> list() {
        return eventRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(event -> EventResponse.from(event, eventMemoryRepository.countByEventId(event.getId())))
                .toList();
    }

    public EventDetailResponse getDetail(UUID id) {
        Event event = getVisible(id);
        Instant now = Instant.now(clock);

        List<UUID> memoryIds = eventMemoryRepository.findByEventId(id).stream()
                .map(EventMemory::getMemoryId)
                .toList();

        Set<UUID> withAttachment = Set.copyOf(
                attachmentRepository.findByMemoryIdIn(memoryIds).stream()
                        .map(dev.solaris.memory.domain.Attachment::getMemoryId)
                        .toList());

        List<MemoryResponse> memories = memoryRepository.findAllById(memoryIds).stream()
                .filter(m -> m.getStatus() != dev.solaris.memory.domain.MemoryStatus.DELETED)
                .sorted(Comparator.comparing(EventService::occurredAt))
                .map(m -> MemoryResponse.from(m, now, withAttachment.contains(m.getId())))
                .toList();

        return EventDetailResponse.from(event, memories);
    }

    @Transactional
    public void addMemory(UUID eventId, UUID memoryId) {
        getVisible(eventId);
        memoryService.getVisible(memoryId);
        if (eventMemoryRepository.existsByEventIdAndMemoryId(eventId, memoryId)) {
            return;
        }
        eventMemoryRepository.save(new EventMemory(UUID.randomUUID(), eventId, memoryId, Instant.now(clock)));
    }

    @Transactional
    public void removeMemory(UUID eventId, UUID memoryId) {
        getVisible(eventId);
        eventMemoryRepository.deleteByEventIdAndMemoryId(eventId, memoryId);
    }

    /** Brief listing for "esta memória pertence a: ..." on the Memory Detail screen. */
    public List<EventResponse> listEventsForMemory(UUID memoryId) {
        memoryService.getVisible(memoryId);
        return eventMemoryRepository.findByMemoryId(memoryId).stream()
                .map(EventMemory::getEventId)
                .distinct()
                .map(this::getVisible)
                .sorted(Comparator.comparing(Event::getCreatedAt).reversed())
                .map(event -> EventResponse.from(event, eventMemoryRepository.countByEventId(event.getId())))
                .toList();
    }

    /** occurredAt == eventAt when set, else createdAt — the spec's exact fallback rule. */
    private static Instant occurredAt(Memory memory) {
        return memory.getEventAt() != null ? memory.getEventAt() : memory.getCreatedAt();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
