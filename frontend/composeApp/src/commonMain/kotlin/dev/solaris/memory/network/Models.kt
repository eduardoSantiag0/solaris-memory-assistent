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
)

@Serializable
data class ApiError(val timestamp: String, val status: Int, val error: String, val message: String, val details: List<String> = emptyList())
