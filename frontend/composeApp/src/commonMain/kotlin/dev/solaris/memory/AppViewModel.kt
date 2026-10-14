package dev.solaris.memory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.solaris.memory.network.ApiClient
import dev.solaris.memory.network.ChatQueryResponse
import dev.solaris.memory.network.CreateMemoryRequest
import dev.solaris.memory.network.MemoryResponse
import dev.solaris.memory.network.SourceRef
import dev.solaris.memory.network.UpdateMemoryRequest
import dev.solaris.memory.network.randomRequestId
import dev.solaris.memory.settings.AppSettings
import dev.solaris.memory.theme.Palette
import dev.solaris.memory.theme.ThemeMode
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class ChatRole { USER, ASSISTANT }

data class ChatEntry(
    val role: ChatRole,
    val text: String,
    val horario: String,
    val caption: String? = null,
    val sensivel: Boolean = false,
    /** HANDOFF.md v1.1 "Linha de fonte" — presente só nas respostas a perguntas, não nas confirmações de gravação. */
    val fonte: SourceRef? = null,
)

/** NovaMemoriaSheet (telas 04/06) reports the form's resolved state, used verbatim both to create and to edit a memory. */
data class NovaMemoriaFormResultado(
    val conteudo: String,
    val ttlDays: Int?,
    val tornarPermanente: Boolean,
    val eventAt: String?,
    val removerEventAt: Boolean,
    val sensivel: Boolean,
)

private fun agora(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
}

/**
 * Deliberately not a DI-framework ViewModel - this is still an early UI
 * slice. [AppSettings] persists dark mode, server URL and the session
 * token to disk (SharedPreferences on Android, java.util.prefs on
 * Desktop) so they survive closing the app; everything else here
 * (chat history, the memories list) is still in-memory only by design -
 * reloading them from the server on each launch is simpler than caching
 * and keeps them from going stale.
 */
class AppViewModel(private val settings: AppSettings) {
    var themeMode by mutableStateOf(ThemeMode.valueOf(settings.themeMode.uppercase()))
        private set
    var palette by mutableStateOf(Palette.entries.first { it.id == settings.paletteId })
        private set
    var serverUrl by mutableStateOf(settings.serverUrl)
    var isLoggedIn by mutableStateOf(false)
    var loginError by mutableStateOf<String?>(null)
    var isRestoringSession by mutableStateOf(settings.token != null)

    var chatHistory by mutableStateOf<List<ChatEntry>>(emptyList())
    var memories by mutableStateOf<List<MemoryResponse>>(emptyList())
    var statusMessage by mutableStateOf<String?>(null)

    val api = ApiClient(serverUrl)

    fun updateThemeMode(value: ThemeMode) {
        themeMode = value
        settings.themeMode = value.name.lowercase()
    }

    fun updatePalette(value: Palette) {
        palette = value
        settings.paletteId = value.id
    }

    fun applyServerUrl() {
        api.setBaseUrl(serverUrl)
        settings.serverUrl = serverUrl
    }

    /** Called once at startup: if a session was saved, try it before falling back to the login screen. */
    suspend fun tryRestoreSession() {
        val savedToken = settings.token ?: return
        isRestoringSession = true
        try {
            applyServerUrl()
            api.restoreToken(savedToken)
            memories = api.listMemories(status = "all").items
            isLoggedIn = true
        } catch (e: Exception) {
            // A saved token that no longer works (expired, revoked, server
            // changed) should fall back to login, not sit on a broken session.
            settings.token = null
        } finally {
            isRestoringSession = false
        }
    }

    suspend fun login(username: String, password: String) {
        loginError = null
        try {
            applyServerUrl()
            api.login(username, password)
            settings.token = api.currentToken()
            isLoggedIn = true
            refreshMemories()
        } catch (e: Exception) {
            loginError = e.message ?: "Falha ao autenticar"
        }
    }

    fun logout() {
        api.logout()
        settings.token = null
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

    /**
     * Used by NovaMemoriaSheet (tela 04) — a structured POST, not text sent
     * through the chat's command parser (HANDOFF.md 5.1: the app decides
     * what gets stored, never the model). Returns an error message on
     * failure so the sheet can stay open with the text preserved, instead
     * of closing early (RF-10, RNF-06: never claim success before the
     * server confirms it).
     */
    suspend fun createMemoryFromForm(content: String, ttlDays: Int?, eventAt: String?, sensitive: Boolean): String? {
        return try {
            val created = api.createMemory(
                CreateMemoryRequest(
                    content = content,
                    ttlDays = ttlDays,
                    eventAt = eventAt,
                    sensitivity = if (sensitive) "SENSITIVE" else null,
                ),
            )
            refreshMemories()
            val confirmacao = if (ttlDays != null) {
                "Salva como temporária. Vale até ${created.expiresAt}."
            } else {
                "Salva como permanente."
            }
            chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, confirmacao, agora())
            null
        } catch (e: Exception) {
            e.message ?: "Falha ao salvar — tente de novo."
        }
    }

    /** Used by NovaMemoriaSheet in edit mode (tela 06, "Editar"). Same RNF-06 contract as createMemoryFromForm. */
    suspend fun updateMemoryFromForm(id: String, resultado: NovaMemoriaFormResultado): String? {
        return try {
            api.updateMemory(
                id,
                UpdateMemoryRequest(
                    content = resultado.conteudo,
                    ttlDays = resultado.ttlDays,
                    makePermanent = resultado.tornarPermanente,
                    eventAt = resultado.eventAt,
                    clearEventAt = resultado.removerEventAt,
                    sensitivity = if (resultado.sensivel) "SENSITIVE" else "NORMAL",
                ),
            )
            refreshMemories()
            null
        } catch (e: Exception) {
            e.message ?: "Falha ao salvar — tente de novo."
        }
    }

    /** Parses /add and /add-temp the same way the backend expects them translated (doc section 4/8) - the app, not the model, decides what gets stored. */
    suspend fun sendChatInput(rawInput: String) {
        val input = rawInput.trim()
        if (input.isEmpty()) return

        chatHistory = chatHistory + ChatEntry(ChatRole.USER, input, agora())

        try {
            when {
                input.startsWith("/add-temp") -> {
                    val rest = input.removePrefix("/add-temp").trim()
                    val parts = rest.split(" ", limit = 2)
                    val days = parts.getOrNull(0)?.toIntOrNull()
                    val text = parts.getOrNull(1)?.trim().orEmpty()
                    if (days == null || days <= 0 || text.isEmpty()) {
                        chatHistory = chatHistory + ChatEntry(
                            ChatRole.ASSISTANT, "Uso: /add-temp <dias> <texto>. Exemplo: /add-temp 2 Deixei a chave na cozinha.", agora(),
                        )
                        return
                    }
                    val created = api.createMemory(CreateMemoryRequest(content = text, ttlDays = days))
                    refreshMemories()
                    chatHistory = chatHistory + ChatEntry(
                        ChatRole.ASSISTANT, "Memória temporária criada.", agora(), caption = "válida até ${created.expiresAt}",
                    )
                }
                input.startsWith("/add") -> {
                    val text = input.removePrefix("/add").trim()
                    if (text.isEmpty()) {
                        chatHistory = chatHistory + ChatEntry(
                            ChatRole.ASSISTANT, "Uso: /add <texto>. Exemplo: /add Meu documento fica na gaveta azul.", agora(),
                        )
                        return
                    }
                    api.createMemory(CreateMemoryRequest(content = text))
                    refreshMemories()
                    chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, "Memória permanente criada.", agora())
                }
                else -> {
                    val response: ChatQueryResponse = api.chatQuery(input, randomRequestId())
                    val melhorFonte = response.sources.firstOrNull()
                    val sensivel = melhorFonte?.sensitivity == "SENSITIVE"
                    chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, response.answer, agora(), sensivel = sensivel, fonte = melhorFonte)
                    response.warnings.forEach { warning ->
                        chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, "aviso: $warning", agora())
                    }
                }
            }
        } catch (e: Exception) {
            chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, "Erro: ${e.message ?: "falha de comunicação com o servidor"}", agora())
        }
    }
}
