package dev.solaris.memory.web;

import dev.solaris.memory.domain.Attachment;
import dev.solaris.memory.domain.Memory;
import dev.solaris.memory.domain.Sensitivity;
import dev.solaris.memory.dto.AttachmentResponse;
import dev.solaris.memory.dto.CreateMemoryRequest;
import dev.solaris.memory.dto.EventResponse;
import dev.solaris.memory.dto.MemoryResponse;
import dev.solaris.memory.dto.PageResponse;
import dev.solaris.memory.dto.UpdateMemoryRequest;
import dev.solaris.memory.repository.AttachmentRepository;
import dev.solaris.memory.service.AttachmentService;
import dev.solaris.memory.service.EventService;
import dev.solaris.memory.service.MemoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/memories")
public class MemoryController {

    private final MemoryService service;
    private final AttachmentService attachmentService;
    private final AttachmentRepository attachmentRepository;
    private final EventService eventService;
    private final Clock clock;

    public MemoryController(MemoryService service, AttachmentService attachmentService,
                             AttachmentRepository attachmentRepository, EventService eventService, Clock clock) {
        this.service = service;
        this.attachmentService = attachmentService;
        this.attachmentRepository = attachmentRepository;
        this.eventService = eventService;
        this.clock = clock;
    }

    @PostMapping
    public ResponseEntity<MemoryResponse> create(@Valid @RequestBody CreateMemoryRequest request) {
        var result = service.create(request);
        MemoryResponse body = toResponse(result.memory(), Instant.now(clock));
        return ResponseEntity.status(result.newlyCreated() ? HttpStatus.CREATED : HttpStatus.OK).body(body);
    }

    /**
     * Nova Memória sheet, paperclip flow (RF: "Opção de colocar imagens ou
     * PDFs e um texto descrevendo ela") — memory + its one attachment are
     * created together in a single request. The plain JSON {@code POST
     * /api/v1/memories} above is untouched and keeps serving the chat
     * {@code /add*} commands, which never carry a file.
     */
    @PostMapping(path = "/with-attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemoryResponse> createWithAttachment(
            @RequestParam String content,
            @RequestParam(required = false) Integer ttlDays,
            // Parsed manually (not as @RequestParam Instant) to match exactly
            // what the JSON endpoint accepts, instead of depending on Spring
            // MVC's implicit String->Instant conversion for a multipart field.
            @RequestParam(required = false) String eventAt,
            @RequestParam(required = false) Sensitivity sensitivity,
            @RequestParam(required = false) String clientRequestId,
            @RequestParam(required = false) String attachmentDescription,
            @RequestParam(required = false) MultipartFile file) {
        var request = new CreateMemoryRequest(content, ttlDays, parseInstant(eventAt), sensitivity, clientRequestId);
        var result = service.createWithAttachment(request, file, attachmentDescription);
        MemoryResponse body = toResponse(result.memory(), Instant.now(clock));
        return ResponseEntity.status(result.newlyCreated() ? HttpStatus.CREATED : HttpStatus.OK).body(body);
    }

    @GetMapping
    public PageResponse<MemoryResponse> list(
            @RequestParam(defaultValue = "ACTIVE") MemoryService.StatusFilter status,
            @RequestParam(defaultValue = "ALL") MemoryService.TypeFilter type,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        Instant now = Instant.now(clock);
        var page = service.list(status, type, q, pageable);
        var withAttachment = attachmentRepository.findByMemoryIdIn(page.getContent().stream().map(Memory::getId).toList())
                .stream().map(Attachment::getMemoryId).collect(java.util.stream.Collectors.toSet());
        return PageResponse.from(page, memory -> MemoryResponse.from(memory, now, withAttachment.contains(memory.getId())));
    }

    @GetMapping("/{id}")
    public MemoryResponse get(@PathVariable UUID id) {
        Memory memory = service.getVisible(id);
        return toResponse(memory, Instant.now(clock));
    }

    @PatchMapping("/{id}")
    public MemoryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateMemoryRequest request) {
        Memory memory = service.update(id, request);
        return toResponse(memory, Instant.now(clock));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/attachment")
    public AttachmentResponse getAttachment(@PathVariable UUID id) {
        return AttachmentResponse.from(attachmentService.getForMemory(id));
    }

    @GetMapping("/{id}/attachment/file")
    public ResponseEntity<byte[]> getAttachmentFile(@PathVariable UUID id) {
        Attachment attachment = attachmentService.getForMemory(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getFilename() + "\"")
                .body(attachment.getData());
    }

    // POST, not PUT: Tomcat (like most servlet containers) only parses a
    // multipart/form-data body via request.getParts() for POST requests —
    // a PUT here reaches the handler with no parts resolved and 500s.
    @PostMapping(path = "/{id}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AttachmentResponse replaceAttachment(@PathVariable UUID id,
                                                 @RequestParam(required = false) String description,
                                                 @RequestParam(required = false) MultipartFile file) {
        service.getVisible(id);
        return AttachmentResponse.from(attachmentService.createFor(id, file, description));
    }

    @DeleteMapping("/{id}/attachment")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID id) {
        attachmentService.deleteForMemory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/events")
    public List<EventResponse> eventsFor(@PathVariable UUID id) {
        return eventService.listEventsForMemory(id);
    }

    private MemoryResponse toResponse(Memory memory, Instant now) {
        return MemoryResponse.from(memory, now, attachmentRepository.existsByMemoryId(memory.getId()));
    }

    private static Instant parseInstant(String value) {
        return (value == null || value.isBlank()) ? null : Instant.parse(value);
    }
}
