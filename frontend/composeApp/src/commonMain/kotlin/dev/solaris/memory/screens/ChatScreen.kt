package dev.solaris.memory.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.ChatRole
import dev.solaris.memory.components.AssistantMessageBody
import dev.solaris.memory.components.AssistantMessageMeta
import dev.solaris.memory.components.ComandoAutocomplete
import dev.solaris.memory.components.Composer
import dev.solaris.memory.components.MaskedValue
import dev.solaris.memory.components.NovaMemoriaSheet
import dev.solaris.memory.components.SourceLink
import dev.solaris.memory.components.UserMessage
import dev.solaris.memory.components.deveMostrarAutocomplete
import dev.solaris.memory.theme.LocalSolarisColors
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
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
                .background(colors.fundo)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                items(viewModel.chatHistory) { entry ->
                    when (entry.role) {
                        ChatRole.USER -> UserMessage(entry.text)
                        ChatRole.ASSISTANT -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            AssistantMessageMeta(entry.horario)
                            if (entry.sensivel) {
                                MaskedValue(entry.text, viewModel)
                            } else {
                                AssistantMessageBody(entry.text)
                            }
                            val fonte = entry.fonte
                            if (fonte != null) {
                                SourceLink(
                                    texto = "FICHA ${fonte.id.takeLast(4).uppercase()} · ${formatarDataHoraCurta(fonte.date)}",
                                    onClick = { onAbrirDetalhe(fonte.id) },
                                )
                            }
                            if (entry.caption != null) {
                                AssistantMessageMeta(entry.caption)
                            }
                        }
                    }
                }
            }

            if (deveMostrarAutocomplete(input)) {
                ComandoAutocomplete(
                    input = input,
                    onSelecionar = { input = it },
                    modifier = Modifier.padding(bottom = 8.dp),
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
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        if (mostrarNovaMemoria) {
            NovaMemoriaSheet(
                onDismiss = { mostrarNovaMemoria = false },
                onSalvar = { resultado ->
                    viewModel.createMemoryFromForm(resultado.conteudo, resultado.ttlDays, resultado.eventAt, resultado.sensivel)
                },
            )
        }
    }
}

private fun formatarDataHoraCurta(iso: String): String = try {
    val dt = Instant.parse(iso).toLocalDateTime(TimeZone.currentSystemDefault())
    "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')} · " +
        "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
} catch (e: Exception) {
    iso
}
