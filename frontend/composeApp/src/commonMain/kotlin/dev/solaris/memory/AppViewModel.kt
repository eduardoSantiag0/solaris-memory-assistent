package dev.solaris.memory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.solaris.memory.components.ChatRole
import dev.solaris.memory.network.ApiClient
import dev.solaris.memory.network.ChatQueryResponse
import dev.solaris.memory.network.CreateMemoryRequest
import dev.solaris.memory.network.MemoryResponse
import dev.solaris.memory.network.randomRequestId

data class ChatEntry(val role: ChatRole, val text: String, val caption: String? = null)

/**
 * Deliberately not a persisted/DI-framework ViewModel yet - this is the
 * first UI slice. Dark mode preference and server URL are in-memory only
 * for now; wiring them to platform-local storage (DataStore/Preferences on
 * Android, a prefs file on Desktop) is a follow-up, not a v1 requirement.
 */
class AppViewModel {
    var darkMode by mutableStateOf(false)
    var serverUrl by mutableStateOf("http://localhost:8080")
    var isLoggedIn by mutableStateOf(false)
    var loginError by mutableStateOf<String?>(null)

    var chatHistory by mutableStateOf<List<ChatEntry>>(emptyList())
    var memories by mutableStateOf<List<MemoryResponse>>(emptyList())
    var statusMessage by mutableStateOf<String?>(null)

    val api = ApiClient(serverUrl)

    fun applyServerUrl() {
        api.setBaseUrl(serverUrl)
    }

    suspend fun login(username: String, password: String) {
        loginError = null
        try {
            applyServerUrl()
            api.login(username, password)
            isLoggedIn = true
            refreshMemories()
        } catch (e: Exception) {
            loginError = e.message ?: "Falha ao autenticar"
        }
    }

    fun logout() {
        api.logout()
        isLoggedIn = false
        chatHistory = emptyList()
        memories = emptyList()
    }

    suspend fun refreshMemories() {
        try {
            val page = api.listMemories(status = "all")
            memories = page.items
        } catch (e: Exception) {
            statusMessage = e.message
        }
    }

    suspend fun deleteMemory(id: String) {
        try {
            api.deleteMemory(id)
            refreshMemories()
        } catch (e: Exception) {
            statusMessage = e.message
        }
    }

    /** Parses /add and /add-temp the same way the backend expects them translated (doc section 4/8) - the app, not the model, decides what gets stored. */
    suspend fun sendChatInput(rawInput: String) {
        val input = rawInput.trim()
        if (input.isEmpty()) return

        chatHistory = chatHistory + ChatEntry(ChatRole.USER, input)

        try {
            when {
                input.startsWith("/add-temp") -> {
                    val rest = input.removePrefix("/add-temp").trim()
                    val parts = rest.split(" ", limit = 2)
                    val days = parts.getOrNull(0)?.toIntOrNull()
                    val text = parts.getOrNull(1)?.trim().orEmpty()
                    if (days == null || days <= 0 || text.isEmpty()) {
                        chatHistory = chatHistory + ChatEntry(
                            ChatRole.ASSISTANT,
                            "Uso: /add-temp <dias> <texto>. Exemplo: /add-temp 2 Deixei a chave na cozinha.",
                        )
                        return
                    }
                    val created = api.createMemory(CreateMemoryRequest(content = text, ttlDays = days))
                    refreshMemories()
                    chatHistory = chatHistory + ChatEntry(
                        ChatRole.ASSISTANT,
                        "Memoria temporaria criada.",
                        caption = "valida até ${created.expiresAt}",
                    )
                }
                input.startsWith("/add") -> {
                    val text = input.removePrefix("/add").trim()
                    if (text.isEmpty()) {
                        chatHistory = chatHistory + ChatEntry(
                            ChatRole.ASSISTANT,
                            "Uso: /add <texto>. Exemplo: /add Meu documento fica na gaveta azul.",
                        )
                        return
                    }
                    api.createMemory(CreateMemoryRequest(content = text))
                    refreshMemories()
                    chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, "Memoria permanente criada.")
                }
                else -> {
                    val response: ChatQueryResponse = api.chatQuery(input, randomRequestId())
                    val caption = response.sources.firstOrNull()?.let { "fonte: ${it.date}" }
                    chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, response.answer, caption)
                    response.warnings.forEach { warning ->
                        chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, "aviso: $warning")
                    }
                }
            }
        } catch (e: Exception) {
            chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, "Erro: ${e.message ?: "falha de comunicacao com o servidor"}")
        }
    }
}
