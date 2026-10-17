package dev.solaris.memory.network

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val expiresAt: String)

@Serializable
data class CreateMemoryRequest(
    val content: String,
    val ttlDays: Int? = null,
    val eventAt: String? = null,
    /** "NORMAL" | "SENSITIVE" — matches the backend's Sensitivity enum name. */
    val sensitivity: String? = null,
    val clientRequestId: String? = null,
)

@Serializable
data class UpdateMemoryRequest(
    val content: String? = null,
    val ttlDays: Int? = null,
    val makePermanent: Boolean = false,
    val eventAt: String? = null,
    val clearEventAt: Boolean = false,
    /** "NORMAL" | "SENSITIVE" | null (null keeps the current value). */
    val sensitivity: String? = null,
)

@Serializable
data class MemoryResponse(
    val id: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val eventAt: String?,
    val expiresAt: String?,
    val status: String, // ACTIVE | EXPIRED | DELETED
    val sensitivity: String?,
    val hasAttachment: Boolean = false,
)

/** Metadata only — raw bytes come from a separate call (ApiClient.getAttachmentBytes). */
@Serializable
data class AttachmentResponse(
    val id: String,
    val memoryId: String,
    val filename: String,
    val contentType: String,
    val description: String,
    val sizeBytes: Long,
    val createdAt: String,
)

@Serializable
data class CreateEventRequest(val title: String, val description: String? = null)

@Serializable
data class UpdateEventRequest(val title: String? = null, val description: String? = null, val clearDescription: Boolean = false)

@Serializable
data class EventResponse(
    val id: String,
    val title: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String,
    val memoryCount: Long,
    val firstOccurredAt: String? = null,
    val lastOccurredAt: String? = null,
)

@Serializable
data class EventDetailResponse(
    val id: String,
    val title: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String,
    val memories: List<MemoryResponse>,
)

@Serializable
data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

@Serializable
data class ChatQueryRequest(val question: String, val clientRequestId: String? = null)

@Serializable
data class SourceRef(val id: String, val snippet: String, val date: String, val sensitivity: String? = null)

@Serializable
data class ChatQueryResponse(
    val answer: String,
    val sources: List<SourceRef>,
    val found: Boolean,
    val warnings: List<String>,
    /** Correções 14/10 item 5 — true when [answer] is already the full "qual vale?" sentence; render as-is, one SourceLink per entry in [sources], never wrap it. */
    val conflict: Boolean = false,
)

@Serializable
data class ApiError(val timestamp: String, val status: Int, val error: String, val message: String, val details: List<String> = emptyList())
