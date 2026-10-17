package dev.solaris.memory.service;

import dev.solaris.memory.config.AppAttachmentProperties;
import dev.solaris.memory.domain.Attachment;
import dev.solaris.memory.exception.AttachmentNotFoundException;
import dev.solaris.memory.exception.InvalidCommandException;
import dev.solaris.memory.repository.AttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class AttachmentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "application/pdf"
    );

    private final AttachmentRepository repository;
    private final AppAttachmentProperties properties;
    private final Clock clock;

    public AttachmentService(AttachmentRepository repository, AppAttachmentProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public Attachment createFor(UUID memoryId, MultipartFile file, String description) {
        validate(file, description);
        // Unique per memory for now (RF: one paperclip in the Nova Memória
        // sheet) — a second upload replaces whatever was there before.
        // Updated in place rather than delete-then-insert: Hibernate flushes
        // insertions before deletions regardless of call order, so a
        // delete+save here would still violate uq_attachments_memory_id at
        // flush time.
        Instant now = Instant.now(clock);
        Attachment attachment = repository.findByMemoryId(memoryId).orElse(null);
        if (attachment != null) {
            attachment.replace(originalFilenameOf(file), file.getContentType(), description.trim(), readBytes(file), now);
            return attachment;
        }
        Attachment created = new Attachment(
                UUID.randomUUID(),
                memoryId,
                originalFilenameOf(file),
                file.getContentType(),
                description.trim(),
                readBytes(file),
                now
        );
        return repository.save(created);
    }

    public Attachment getForMemory(UUID memoryId) {
        return repository.findByMemoryId(memoryId).orElseThrow(() -> new AttachmentNotFoundException(memoryId));
    }

    @Transactional
    public void deleteForMemory(UUID memoryId) {
        repository.deleteByMemoryId(memoryId);
    }

    private void validate(MultipartFile file, String description) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCommandException(
                    "O anexo precisa de um arquivo. Exemplo: envie a imagem/PDF no campo \"file\".");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidCommandException(
                    "O anexo precisa de uma descrição. Exemplo: {\"attachmentDescription\": \"Recibo da consulta\"}");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidCommandException(
                    "Tipo de arquivo não suportado — envie uma imagem (jpeg/png/webp/gif) ou um PDF.");
        }
        long maxBytes = properties.getMaxSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new InvalidCommandException(
                    "Arquivo maior que o limite de " + properties.getMaxSizeMb() + "MB.");
        }
    }

    private static String originalFilenameOf(MultipartFile file) {
        String name = file.getOriginalFilename();
        return (name == null || name.isBlank()) ? "anexo" : name;
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new InvalidCommandException("Falha ao ler o arquivo enviado.");
        }
    }
}
