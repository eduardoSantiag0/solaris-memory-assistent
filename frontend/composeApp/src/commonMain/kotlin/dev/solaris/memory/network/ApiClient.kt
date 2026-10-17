package dev.solaris.memory.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Thrown with the server's own message so screens can show it directly instead of a generic failure. */
class ApiException(message: String) : Exception(message)

/** Talks to the solaris-memory-assistent backend (see ../../../backend). */
class ApiClient(private var baseUrl: String) {

    private var token: String? = null

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    fun setBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }

    fun isAuthenticated(): Boolean = token != null

    /** Reapplies a token persisted from a previous session (dev/solaris/memory/settings) without re-authenticating. */
    fun restoreToken(savedToken: String) {
        token = savedToken
    }

    fun currentToken(): String? = token

    suspend fun login(username: String, password: String) {
        val response = client.post("$baseUrl/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }
        token = parseOrThrow<LoginResponse>(response).token
    }

    fun logout() {
        token = null
    }

    suspend fun createMemory(request: CreateMemoryRequest): MemoryResponse {
        val response = client.post("$baseUrl/api/v1/memories") {
            authHeader()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return parseOrThrow(response)
    }

    suspend fun listMemories(status: String = "active", query: String? = null): PageResponse<MemoryResponse> {
        val response = client.get("$baseUrl/api/v1/memories") {
            authHeader()
            url {
                parameters.append("status", status)
                if (!query.isNullOrBlank()) parameters.append("q", query)
            }
        }
        return parseOrThrow(response)
    }

    suspend fun updateMemory(id: String, request: UpdateMemoryRequest): MemoryResponse {
        val response = client.patch("$baseUrl/api/v1/memories/$id") {
            authHeader()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return parseOrThrow(response)
    }

    suspend fun deleteMemory(id: String) {
        val response = client.delete("$baseUrl/api/v1/memories/$id") {
            authHeader()
        }
        if (!response.status.isSuccess()) {
            throw ApiException(errorMessageOf(response))
        }
    }

    /** Nova Memória sheet, paperclip flow: memory + its one attachment created together in one request. */
    suspend fun createMemoryWithAttachment(
        content: String,
        ttlDays: Int?,
        eventAt: String?,
        sensitivity: String?,
        clientRequestId: String?,
        attachmentDescription: String,
        fileBytes: ByteArray,
        filename: String,
        mimeType: String,
    ): MemoryResponse {
        val response = client.post("$baseUrl/api/v1/memories/with-attachment") {
            authHeader()
            setBody(MultiPartFormDataContent(attachmentFormParts(
                content, ttlDays, eventAt, sensitivity, clientRequestId, attachmentDescription, fileBytes, filename, mimeType,
            )))
        }
        return parseOrThrow(response)
    }

    suspend fun getAttachment(memoryId: String): AttachmentResponse {
        val response = client.get("$baseUrl/api/v1/memories/$memoryId/attachment") { authHeader() }
        return parseOrThrow(response)
    }

    suspend fun getAttachmentBytes(memoryId: String): ByteArray {
        val response = client.get("$baseUrl/api/v1/memories/$memoryId/attachment/file") { authHeader() }
        if (!response.status.isSuccess()) {
            throw ApiException(errorMessageOf(response))
        }
        return response.body()
    }

    suspend fun replaceAttachment(memoryId: String, description: String, fileBytes: ByteArray, filename: String, mimeType: String): AttachmentResponse {
        // POST, not PUT — the backend's multipart parsing only works for POST (see MemoryController).
        val response = client.post("$baseUrl/api/v1/memories/$memoryId/attachment") {
            authHeader()
            setBody(MultiPartFormDataContent(formData {
                append("description", description)
                append("file", fileBytes, filePartHeaders(filename, mimeType))
            }))
        }
        return parseOrThrow(response)
    }

    suspend fun deleteAttachment(memoryId: String) {
        val response = client.delete("$baseUrl/api/v1/memories/$memoryId/attachment") { authHeader() }
        if (!response.status.isSuccess()) {
            throw ApiException(errorMessageOf(response))
        }
    }

    suspend fun eventsForMemory(memoryId: String): List<EventResponse> {
        val response = client.get("$baseUrl/api/v1/memories/$memoryId/events") { authHeader() }
        return parseOrThrow(response)
    }

    suspend fun createEvent(request: CreateEventRequest): EventResponse {
        val response = client.post("$baseUrl/api/v1/events") {
            authHeader()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return parseOrThrow(response)
    }

    suspend fun listEvents(): List<EventResponse> {
        val response = client.get("$baseUrl/api/v1/events") { authHeader() }
        return parseOrThrow(response)
    }

    suspend fun getEvent(id: String): EventDetailResponse {
        val response = client.get("$baseUrl/api/v1/events/$id") { authHeader() }
        return parseOrThrow(response)
    }

    suspend fun updateEvent(id: String, request: UpdateEventRequest): EventResponse {
        val response = client.patch("$baseUrl/api/v1/events/$id") {
            authHeader()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return parseOrThrow(response)
    }

    suspend fun deleteEvent(id: String) {
        val response = client.delete("$baseUrl/api/v1/events/$id") { authHeader() }
        if (!response.status.isSuccess()) {
            throw ApiException(errorMessageOf(response))
        }
    }

    suspend fun addMemoryToEvent(eventId: String, memoryId: String) {
        val response = client.post("$baseUrl/api/v1/events/$eventId/memories/$memoryId") { authHeader() }
        if (!response.status.isSuccess()) {
            throw ApiException(errorMessageOf(response))
        }
    }

    suspend fun removeMemoryFromEvent(eventId: String, memoryId: String) {
        val response = client.delete("$baseUrl/api/v1/events/$eventId/memories/$memoryId") { authHeader() }
        if (!response.status.isSuccess()) {
            throw ApiException(errorMessageOf(response))
        }
    }

    private fun filePartHeaders(filename: String, mimeType: String): Headers = Headers.build {
        append(HttpHeaders.ContentType, mimeType)
        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
    }

    private fun attachmentFormParts(
        content: String,
        ttlDays: Int?,
        eventAt: String?,
        sensitivity: String?,
        clientRequestId: String?,
        attachmentDescription: String,
        fileBytes: ByteArray,
        filename: String,
        mimeType: String,
    ) = formData {
        append("content", content)
        ttlDays?.let { append("ttlDays", it.toString()) }
        eventAt?.let { append("eventAt", it) }
        sensitivity?.let { append("sensitivity", it) }
        clientRequestId?.let { append("clientRequestId", it) }
        append("attachmentDescription", attachmentDescription)
        append("file", fileBytes, filePartHeaders(filename, mimeType))
    }

    suspend fun chatQuery(question: String, clientRequestId: String): ChatQueryResponse {
        val response = client.post("$baseUrl/api/v1/chat/query") {
            authHeader()
            contentType(ContentType.Application.Json)
            setBody(ChatQueryRequest(question, clientRequestId))
        }
        return parseOrThrow(response)
    }

    private fun HttpRequestBuilder.authHeader() {
        token?.let { headers { append("Authorization", "Bearer $it") } }
    }

    private suspend inline fun <reified T> parseOrThrow(response: HttpResponse): T {
        if (!response.status.isSuccess()) {
            throw ApiException(errorMessageOf(response))
        }
        return response.body()
    }

    private suspend fun errorMessageOf(response: HttpResponse): String {
        return try {
            response.body<ApiError>().message
        } catch (e: Exception) {
            "Erro ${response.status.value}"
        }
    }

    private fun HttpStatusCode.isSuccess() = value in 200..299
}
