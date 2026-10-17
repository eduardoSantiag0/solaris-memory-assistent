package dev.solaris.memory.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.ChatRole
import dev.solaris.memory.components.AssistantMessageBody
import dev.solaris.memory.components.AssistantMessageMeta
import dev.solaris.memory.components.ComandoAutocomplete
import dev.solaris.memory.components.Composer
import dev.solaris.memory.components.MaskedValue
import dev.solaris.memory.components.NovaMemoriaSheet
import dev.solaris.memory.components.SavedMessage
import dev.solaris.memory.components.SourceLink
import dev.solaris.memory.components.UserMessage
import dev.solaris.memory.components.deveMostrarAutocomplete
import dev.solaris.memory.components.ditherConversa
import dev.solaris.memory.network.SourceRef
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.solarisText
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

@Composable
fun ChatScreen(viewModel: AppViewModel, onAbrirDetalhe: (String) -> Unit) {
    val colors = LocalSolarisColors.current
    var input by remember { mutableStateOf("") }
    var mostrarNovaMemoria by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.chatHistory.size) {
        if (viewModel.chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.chatHistory.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.fundo),
        ) {
            // A textura precisa cobrir a área rolável inteira, até as bordas
            // da tela — por isso o padding de 20/16dp vira contentPadding da
            // LazyColumn (dentro do Box com a textura) em vez de padding do
            // Column (que deixaria uma faixa sem textura nas margens).
            Box(modifier = Modifier.weight(1f).fillMaxWidth().ditherConversa(colors.textura)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                itemsIndexed(viewModel.chatHistory) { index, entry ->
                    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                        val anterior = viewModel.chatHistory.getOrNull(index - 1)
                        if (anterior == null || anterior.data != entry.data) {
                            Text(
                                "— ${formatarDataSeparador(entry.data)} —",
                                style = solarisText.meta,
                                color = colors.texto,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        }
                        when (entry.role) {
                            ChatRole.USER -> UserMessage(entry.text)
                            ChatRole.ASSISTANT -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                AssistantMessageMeta(entry.horario)
                                val fonte = entry.fonte
                                if (entry.salva) {
                                    SavedMessage(entry.text)
                                } else if (fonte?.sensitivity == "SENSITIVE") {
                                    // Pedido do usuário em 15/10: mascara no chat também, mas
                                    // sem pedir biometria de novo (só o toque em Revelar) —
                                    // diferente do gate de Memórias/Detalhe. A frase "segundo o
                                    // que você registrou..." fica fora da caixa (não é parte do
                                    // valor mascarado); dentro só o conteúdo literal da memória.
                                    Text("Segundo o que você registrou ${relativo(fonte.date)}.", style = solarisText.meta, color = colors.textoSecundario)
                                    MaskedValue(entry.text, viewModel, requireAuth = false)
                                } else {
                                    AssistantMessageBody(respostaConversacional(entry.text, fonte))
                                }
                                if (entry.fontesConflito.isNotEmpty()) {
                                    entry.fontesConflito.forEach { ref ->
                                        SourceLink(
                                            texto = "FICHA ${ref.id.takeLast(4).uppercase()} · ${formatarDataHoraCurta(ref.date)}",
                                            onClick = { onAbrirDetalhe(ref.id) },
                                        )
                                    }
                                } else if (fonte != null) {
                                    SourceLink(
                                        texto = "FICHA ${fonte.id.takeLast(4).uppercase()} · ${formatarDataHoraCurta(fonte.date)}",
                                        onClick = { onAbrirDetalhe(fonte.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }

            if (deveMostrarAutocomplete(input)) {
                ComandoAutocomplete(
                    input = input,
                    onSelecionar = { input = it },
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
                )
            }

            Composer(
                value = input,
                onValueChange = { input = it },
                onNovaMemoria = { mostrarNovaMemoria = true },
                onEnviar = {
                    val toSend = input
                    input = ""
                    scope.launch { viewModel.sendChatInput(toSend) }
                },
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
            )
        }

        if (mostrarNovaMemoria) {
            NovaMemoriaSheet(
                viewModel = viewModel,
                onDismiss = { mostrarNovaMemoria = false },
                onSalvar = { resultado -> viewModel.createMemoryFromForm(resultado) },
            )
        }
    }
}

/** Correções 14/10 item 7: ChatEntry.data já vem como "yyyy-MM-dd" (kotlinx.datetime.LocalDate.toString()). */
private fun formatarDataSeparador(data: String): String = try {
    val (ano, mes, dia) = data.split("-")
    "$dia/$mes/$ano"
} catch (e: Exception) {
    data
}

private fun formatarDataHoraCurta(iso: String): String = try {
    val dt = Instant.parse(iso).toLocalDateTime(TimeZone.currentSystemDefault())
    "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')} · " +
        "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
} catch (e: Exception) {
    iso
}

/**
 * Embrulha o texto literal da memória numa frase de conversa, sem
 * inventar nem resumir nada — RF-04/CT-05/CT-11 continuam garantidos pelo
 * backend (chat/query sempre devolve o conteúdo bruto); isso só adiciona
 * a moldura "..., segundo o que você registrou ontem às 21:40." que a
 * tela 02 do handoff v1.1 já mostra como exemplo.
 */
private fun respostaConversacional(texto: String, fonte: SourceRef?): String {
    if (fonte == null) return texto
    val base = texto.trimEnd().removeSuffix(".")
    return "$base, segundo o que você registrou ${relativo(fonte.date)}."
}

private fun relativo(iso: String): String = try {
    val tz = TimeZone.currentSystemDefault()
    val dt = Instant.parse(iso).toLocalDateTime(tz)
    val hoje = Clock.System.now().toLocalDateTime(tz).date
    val hora = "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    when (dt.date) {
        hoje -> "hoje às $hora"
        hoje.minus(1, DateTimeUnit.DAY) -> "ontem às $hora"
        else -> "em ${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}, às $hora"
    }
} catch (e: Exception) {
    iso
}
