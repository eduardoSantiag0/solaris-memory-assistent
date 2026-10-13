package dev.solaris.memory.web;

import dev.solaris.memory.domain.Memory;
import dev.solaris.memory.dto.CreateMemoryRequest;
import dev.solaris.memory.dto.MemoryResponse;
import dev.solaris.memory.dto.PageResponse;
import dev.solaris.memory.dto.UpdateMemoryRequest;
import dev.solaris.memory.service.MemoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/memories")
public class MemoryController {

    private final MemoryService service;
    private final Clock clock;

    public MemoryController(MemoryService service, Clock clock) {
        this.service = service;
        this.clock = clock;
    }

    @PostMapping
    public ResponseEntity<MemoryResponse> create(@Valid @RequestBody CreateMemoryRequest request) {
        var result = service.create(request);
        MemoryResponse body = MemoryResponse.from(result.memory(), Instant.now(clock));
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
        return PageResponse.from(page, memory -> MemoryResponse.from(memory, now));
    }

    @GetMapping("/{id}")
    public MemoryResponse get(@PathVariable UUID id) {
        Memory memory = service.getVisible(id);
        return MemoryResponse.from(memory, Instant.now(clock));
    }

    @PatchMapping("/{id}")
    public MemoryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateMemoryRequest request) {
        Memory memory = service.update(id, request);
        return MemoryResponse.from(memory, Instant.now(clock));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
