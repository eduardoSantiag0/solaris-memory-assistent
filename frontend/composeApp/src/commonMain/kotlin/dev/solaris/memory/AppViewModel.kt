package dev.solaris.memory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.solaris.memory.components.PickedFile
import dev.solaris.memory.network.ApiClient
import dev.solaris.memory.network.ChatQueryResponse
import dev.solaris.memory.network.CreateEventRequest
import dev.solaris.memory.network.CreateMemoryRequest
import dev.solaris.memory.network.EventDetailResponse
import dev.solaris.memory.network.EventResponse
import dev.solaris.memory.network.MemoryResponse
import dev.solaris.memory.network.SourceRef
import dev.solaris.memory.network.UpdateEventRequest
import dev.solaris.memory.network.UpdateMemoryRequest
import dev.solaris.memory.network.randomRequestId
import dev.solaris.memory.settings.AppSettings
import dev.solaris.memory.theme.Palette
import dev.solaris.memory.theme.ThemeMode
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class ChatRole { USER, ASSISTANT }

data class ChatEntry(
    val role: ChatRole,
    val text: String,
    val horario: String,
    /** Correções 14/10 item 7: data (yyyy-MM-dd) por entrada, só para decidir quando desenhar o separador "— dd/mm/aaaa —" entre turnos de dias diferentes. */
    val data: String = hojeData(),
    /** HANDOFF.md v1.1 "Linha de fonte" — presente só nas respostas a perguntas, não nas confirmações de gravação. */
    val fonte: SourceRef? = null,
    /** Correções 14/10 item 5 — não-vazio só na resposta de conflito: uma SourceLink por ficha, `text` já é a frase inteira ("... Qual vale?"), nunca embrulhada de novo. */
    val fontesConflito: List<SourceRef> = emptyList(),
    /** HANDOFF.md v1.1 "Confirmação de gravação" — renderiza com a bandeirinha (components/SavedMessage) em vez de texto corrido comum. */
    val salva: Boolean = false,
)

/** Correções 14/10 item 6: textos exatos de HANDOFF.md 5.5 — sempre "dd/mm, HH:mm", nunca o ISO bruto. */
private fun formatarDataHoraVirgula(iso: String): String = try {
    val dt = Instant.parse(iso).toLocalDateTime(TimeZone.currentSystemDefault())
    "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}, " +
        "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
} catch (e: Exception) {
    iso
}

/** NovaMemoriaSheet (telas 04/06) reports the form's resolved state, used verbatim both to create and to edit a memory. */
data class NovaMemoriaFormResultado(
    val conteudo: String,
    val ttlDays: Int?,
    val tornarPermanente: Boolean,
    val eventAt: String?,
    val removerEventAt: Boolean,
    val sensivel: Boolean,
    val anexo: PickedFile? = null,
    val descricaoAnexo: String? = null,
    val removerAnexo: Boolean = false,
)

private fun agora(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
}

private fun hojeData(): String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

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
    var requireBiometricOnOpen by mutableStateOf(settings.requireBiometricOnOpen)
        private set
    var blockScreenshots by mutableStateOf(settings.blockScreenshots)
        private set
    var serverUrl by mutableStateOf(settings.serverUrl)
    var isLoggedIn by mutableStateOf(false)
    var loginError by mutableStateOf<String?>(null)
    var isRestoringSession by mutableStateOf(settings.token != null)

    var chatHistory by mutableStateOf<List<ChatEntry>>(emptyList())
    var memories by mutableStateOf<List<MemoryResponse>>(emptyList())
    var events by mutableStateOf<List<EventResponse>>(emptyList())
    var eventoDetalhe by mutableStateOf<EventDetailResponse?>(null)
    var statusMessage by mutableStateOf<String?>(null)
    /**
     * Achado do audit de UI 15/10: o indicador "VPN · OK" no cabeçalho
     * estava sempre fixo em `true`. Aqui vira um sinal real, derivado da
     * última chamada de rede: `ApiException` significa que o servidor
     * respondeu (então a VPN está de pé, só que com erro de outra
     * natureza); qualquer outra exceção (timeout, conexão recusada,
     * DNS/host inalcançável) significa que a chamada nem chegou lá.
     */
    var isConnected by mutableStateOf(true)
        private set

    private fun registrarResultadoDeRede(e: Exception) {
        isConnected = e is dev.solaris.memory.network.ApiException
    }

    val api = ApiClient(serverUrl)

    fun updateThemeMode(value: ThemeMode) {
        themeMode = value
        settings.themeMode = value.name.lowercase()
    }

    fun updatePalette(value: Palette) {
        palette = value
        settings.paletteId = value.id
    }

    fun updateRequireBiometricOnOpen(value: Boolean) {
        requireBiometricOnOpen = value
        settings.requireBiometricOnOpen = value
    }

    fun updateBlockScreenshots(value: Boolean) {
        blockScreenshots = value
        settings.blockScreenshots = value
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
            events = api.listEvents()
            isLoggedIn = true
            isConnected = true
        } catch (e: Exception) {
            // A saved token that no longer works (expired, revoked, server
            // changed) should fall back to login, not sit on a broken session.
            settings.token = null
            registrarResultadoDeRede(e)
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
            settings.username = username
            isLoggedIn = true
            isConnected = true
            refreshMemories()
            refreshEvents()
        } catch (e: Exception) {
            loginError = e.message ?: "Falha ao autenticar"
            registrarResultadoDeRede(e)
        }
    }

    /**
     * HANDOFF.md 5.4 item 2, adaptado para Desktop (sem biometria — ver
     * components/SensitiveGate.desktop.kt): confirma a identidade antes de
     * revelar um valor sensível reenviando a senha para /auth/login com o
     * username já salvo. Não mexe em `isLoggedIn`/`loginError` — é uma
     * checagem pontual, não um novo login.
     */
    suspend fun verificarSenha(password: String): Boolean {
        val username = settings.username ?: return false
        return try {
            api.login(username, password)
            settings.token = api.currentToken()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun logout() {
        api.logout()
        settings.token = null
        settings.username = null
        isLoggedIn = false
        chatHistory = emptyList()
        memories = emptyList()
        events = emptyList()
        eventoDetalhe = null
    }

    /**
     * "Limpar cache" (Ajustes) — o único estado local que se acumula e não
     * vem do servidor é o histórico de conversa (nunca persistido entre
     * sessões, mas cresce indefinidamente dentro de uma sessão). Não afeta
     * nenhuma memória salva no servidor.
     */
    fun clearCache() {
        chatHistory = emptyList()
    }

    suspend fun refreshMemories() {
        try {
            val page = api.listMemories(status = "all")
            memories = page.items
            isConnected = true
        } catch (e: Exception) {
            statusMessage = e.message ?: "Falha ao atualizar a lista de memórias."
            registrarResultadoDeRede(e)
        }
    }

    suspend fun refreshEvents() {
        try {
            events = api.listEvents()
            isConnected = true
        } catch (e: Exception) {
            statusMessage = e.message ?: "Falha ao atualizar a lista de eventos."
            registrarResultadoDeRede(e)
        }
    }

    suspend fun loadEventDetail(id: String) {
        try {
            eventoDetalhe = api.getEvent(id)
            isConnected = true
        } catch (e: Exception) {
            statusMessage = e.message ?: "Falha ao abrir o evento."
            registrarResultadoDeRede(e)
        }
    }

    suspend fun createEvent(title: String, description: String?): String? {
        return try {
            api.createEvent(CreateEventRequest(title, description))
            refreshEvents()
            null
        } catch (e: Exception) {
            e.message ?: "Falha ao criar o evento — tente de novo."
        }
    }

    suspend fun updateEvent(id: String, title: String, description: String?): String? {
        return try {
            api.updateEvent(id, UpdateEventRequest(title = title, description = description, clearDescription = description == null))
            refreshEvents()
            loadEventDetail(id)
            null
        } catch (e: Exception) {
            e.message ?: "Falha ao salvar o evento — tente de novo."
        }
    }

    suspend fun deleteEvent(id: String): Boolean {
        return try {
            api.deleteEvent(id)
            refreshEvents()
            true
        } catch (e: Exception) {
            statusMessage = e.message ?: "Falha ao excluir o evento — tente de novo."
            registrarResultadoDeRede(e)
            false
        }
    }

    suspend fun addMemoryToEvent(eventId: String, memoryId: String) {
        try {
            api.addMemoryToEvent(eventId, memoryId)
            loadEventDetail(eventId)
            refreshEvents()
        } catch (e: Exception) {
            statusMessage = e.message ?: "Falha ao adicionar a memória ao evento."
            registrarResultadoDeRede(e)
        }
    }

    suspend fun removeMemoryFromEvent(eventId: String, memoryId: String) {
        try {
            api.removeMemoryFromEvent(eventId, memoryId)
            loadEventDetail(eventId)
            refreshEvents()
        } catch (e: Exception) {
            statusMessage = e.message ?: "Falha ao remover a memória do evento."
            registrarResultadoDeRede(e)
        }
    }

    /**
     * Devolve se a exclusão de fato aconteceu — o chamador (tela de
     * Detalhe) só deve navegar de volta pra lista quando for `true`.
     * Antes disso a tela voltava pra lista imediatamente e disparava a
     * exclusão em paralelo sem esperar o resultado, então uma falha real
     * (rede, servidor fora) passava batido, igual a um sucesso.
     */
    suspend fun deleteMemory(id: String): Boolean {
        return try {
            api.deleteMemory(id)
            isConnected = true
            refreshMemories()
            true
        } catch (e: Exception) {
            statusMessage = e.message ?: "Falha ao excluir — tente de novo."
            registrarResultadoDeRede(e)
            false
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
    suspend fun createMemoryFromForm(resultado: NovaMemoriaFormResultado): String? {
        val ttlDays = resultado.ttlDays
        val sensitive = resultado.sensivel
        return try {
            val anexo = resultado.anexo
            val created = if (anexo != null) {
                api.createMemoryWithAttachment(
                    content = resultado.conteudo,
                    ttlDays = ttlDays,
                    eventAt = resultado.eventAt,
                    sensitivity = if (sensitive) "SENSITIVE" else null,
                    clientRequestId = null,
                    attachmentDescription = resultado.descricaoAnexo.orEmpty(),
                    fileBytes = anexo.bytes,
                    filename = anexo.name,
                    mimeType = anexo.mimeType,
                )
            } else {
                api.createMemory(
                    CreateMemoryRequest(
                        content = resultado.conteudo,
                        ttlDays = ttlDays,
                        eventAt = resultado.eventAt,
                        sensitivity = if (sensitive) "SENSITIVE" else null,
                    ),
                )
            }
            refreshMemories()
            // Textos exatos de HANDOFF.md 5.5 (Correções 14/10 item 6).
            val confirmacao = when {
                ttlDays != null && sensitive -> "Salva como temporária e sensível. Vale até ${formatarDataHoraVirgula(created.expiresAt!!)}."
                ttlDays != null -> "Salva como temporária. Vale até ${formatarDataHoraVirgula(created.expiresAt!!)}."
                sensitive -> "Salva como permanente e sensível."
                else -> "Salva como permanente."
            }
            chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, confirmacao, agora(), salva = true)
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
            val anexo = resultado.anexo
            if (anexo != null) {
                api.replaceAttachment(id, resultado.descricaoAnexo.orEmpty(), anexo.bytes, anexo.name, anexo.mimeType)
            } else if (resultado.removerAnexo) {
                api.deleteAttachment(id)
            }
            refreshMemories()
            null
        } catch (e: Exception) {
            e.message ?: "Falha ao salvar — tente de novo."
        }
    }

    /**
     * Parses /add, /add-temp and /add-sensitive the same way the backend
     * expects them translated (doc section 4/8) - the app, not the model,
     * decides what gets stored. There is no /add-temp-sensitive: per
     * Correções 14/10 item 9, temporary+sensitive stays form-only (Nova
     * memória), chat only ever approved the three commands in tela 03.
     */
    suspend fun sendChatInput(rawInput: String) {
        val input = rawInput.trim()
        if (input.isEmpty()) return

        chatHistory = chatHistory + ChatEntry(ChatRole.USER, input, agora())

        try {
            when {
                input.startsWith("/add-sensitive") -> {
                    val text = input.removePrefix("/add-sensitive").trim()
                    if (text.isEmpty()) {
                        chatHistory = chatHistory + ChatEntry(
                            ChatRole.ASSISTANT, "Uso: /add-sensitive <texto>. Exemplo: /add-sensitive Meu RG é 12.345.678-9.", agora(),
                        )
                        return
                    }
                    api.createMemory(CreateMemoryRequest(content = text, sensitivity = "SENSITIVE"))
                    refreshMemories()
                    chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, "Salva como permanente e sensível.", agora(), salva = true)
                }
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
                        ChatRole.ASSISTANT, "Salva como temporária. Vale até ${formatarDataHoraVirgula(created.expiresAt!!)}.", agora(), salva = true,
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
                    chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, "Salva como permanente.", agora(), salva = true)
                }
                input.startsWith("/event") -> {
                    val rest = input.removePrefix("/event").trim()
                    val partes = rest.split("|", limit = 2).map { it.trim() }
                    val tituloEvento = partes.getOrNull(0).orEmpty()
                    val texto = partes.getOrNull(1).orEmpty()
                    if (tituloEvento.isEmpty() || texto.isEmpty()) {
                        chatHistory = chatHistory + ChatEntry(
                            ChatRole.ASSISTANT,
                            "Uso: /event <evento> | <texto>. Exemplo: /event Viagem para o Chile | Compramos as passagens hoje.",
                            agora(),
                        )
                        return
                    }
                    refreshEvents()
                    val evento = events.firstOrNull { it.title.equals(tituloEvento, ignoreCase = true) }
                        ?: api.createEvent(CreateEventRequest(title = tituloEvento))
                    val created = api.createMemory(CreateMemoryRequest(content = texto))
                    api.addMemoryToEvent(evento.id, created.id)
                    refreshMemories()
                    refreshEvents()
                    chatHistory = chatHistory + ChatEntry(
                        ChatRole.ASSISTANT, "Salva como permanente e associada ao evento \"${evento.title}\".", agora(), salva = true,
                    )
                }
                else -> {
                    val response: ChatQueryResponse = api.chatQuery(input, randomRequestId())
                    if (response.conflict) {
                        chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, response.answer, agora(), fontesConflito = response.sources)
                        return
                    }
                    val melhorFonte = response.sources.firstOrNull()
                    chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, response.answer, agora(), fonte = melhorFonte)
                    // HANDOFF.md 5.5: nunca prefixar com rótulos técnicos como "aviso:".
                    response.warnings.forEach { warning ->
                        chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, warning, agora())
                    }
                }
            }
            isConnected = true
        } catch (e: Exception) {
            chatHistory = chatHistory + ChatEntry(ChatRole.ASSISTANT, "Erro: ${e.message ?: "falha de comunicação com o servidor"}", agora())
            registrarResultadoDeRede(e)
        }
    }
}
