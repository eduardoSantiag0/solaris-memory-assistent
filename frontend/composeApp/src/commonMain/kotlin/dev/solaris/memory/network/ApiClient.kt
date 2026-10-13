package dev.solaris.memory.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Thrown with the server's own message so screens can show it directly instead of a generic failure. */
class ApiException(message: String) : Exception(message)

/**
 * Talks to the solaris-memory-assistent backend (see ../../../backend). No
 * token persistence yet (v1 keeps it in memory only — added when the
 * Ajustes/session-management screen grows past this first slice).
 */
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
