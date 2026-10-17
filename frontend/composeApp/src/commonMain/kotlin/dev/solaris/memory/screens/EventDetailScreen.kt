package dev.solaris.memory.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.components.ConfirmDialog
import dev.solaris.memory.components.DangerButton
import dev.solaris.memory.components.IconSquareButton
import dev.solaris.memory.components.MemoryCardItem
import dev.solaris.memory.components.NovoEventoDialog
import dev.solaris.memory.components.SecondaryButton
import dev.solaris.memory.components.SelecionarMemoriaSheet
import dev.solaris.memory.network.MemoryResponse
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.solarisText
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Agrupamento manual de memórias (RF: "Agrupamento de memórias em
 * eventos"). A lista já vem ordenada pelo backend — occurredAt
 * (eventAt) com fallback para createdAt — nunca por IA/embeddings.
 */
@Composable
fun EventDetailScreen(eventId: String, viewModel: AppViewModel, onAbrirMemoria: (String) -> Unit, onExcluido: () -> Unit) {
    val colors = LocalSolarisColors.current
    val scope = rememberCoroutineScope()
    var editando by remember { mutableStateOf(false) }
    var adicionandoMemoria by remember { mutableStateOf(false) }
    var confirmandoExclusao by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        viewModel.loadEventDetail(eventId)
    }

    val evento = viewModel.eventoDetalhe
    if (evento == null || evento.id != eventId) {
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(colors.fundo).padding(20.dp)) {
            Text(evento.title, style = solarisText.tituloTela, color = colors.texto)
            evento.description?.let {
                Text(it, style = solarisText.corpo, color = colors.textoSecundario, modifier = Modifier.padding(top = 8.dp))
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                SecondaryButton(text = "Editar", onClick = { editando = true }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                IconSquareButton(SolarisIcon.NOVA, "Adicionar memória", { adicionandoMemoria = true }, preenchido = true)
            }

            Spacer(Modifier.height(20.dp))
            Text("MEMÓRIAS · ${evento.memories.size}", style = solarisText.meta, color = colors.texto)

            if (evento.memories.isEmpty()) {
                Text(
                    "Nenhuma memória neste evento ainda.",
                    style = solarisText.corpo,
                    color = colors.texto,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                itemsIndexed(evento.memories) { index, memory ->
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Eventos longos ficam mais fáceis de escanear com a mesma
                        // quebra por dia usada na Conversa, já que a lista aqui
                        // também é só uma linha do tempo de memórias.
                        val anterior = evento.memories.getOrNull(index - 1)
                        val diaAtual = diaDoOcorrido(memory)
                        if (anterior == null || diaDoOcorrido(anterior) != diaAtual) {
                            Text(
                                "— $diaAtual —",
                                style = solarisText.meta,
                                color = colors.textoSuave,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        }
                        EventMemoryRow(
                            memory = memory,
                            viewModel = viewModel,
                            onClick = { onAbrirMemoria(memory.id) },
                            onRemover = { scope.launch { viewModel.removeMemoryFromEvent(eventId, memory.id) } },
                        )
                    }
                }
            }

            DangerButton(text = "Excluir evento", onClick = { confirmandoExclusao = true }, modifier = Modifier.padding(top = 12.dp, bottom = 12.dp))
        }

        if (editando) {
            NovoEventoDialog(
                tituloInicial = evento.title,
                descricaoInicial = evento.description.orEmpty(),
                editando = true,
                onDismiss = { editando = false },
                onSalvar = { titulo, descricao ->
                    scope.launch {
                        viewModel.updateEvent(eventId, titulo, descricao)
                        editando = false
                    }
                },
            )
        }

        if (adicionandoMemoria) {
            SelecionarMemoriaSheet(
                viewModel = viewModel,
                jaNoEvento = evento.memories.map { it.id }.toSet(),
                onDismiss = { adicionandoMemoria = false },
                onSelecionar = { memory ->
                    scope.launch { viewModel.addMemoryToEvent(eventId, memory.id) }
                    adicionandoMemoria = false
                },
            )
        }

        if (confirmandoExclusao) {
            ConfirmDialog(
                titulo = "Excluir este evento?",
                texto = "As memórias continuam salvas — só o agrupamento é removido. Não dá para desfazer.",
                confirmarLabel = "Excluir",
                onConfirmar = {
                    scope.launch {
                        confirmandoExclusao = false
                        if (viewModel.deleteEvent(eventId)) onExcluido()
                    }
                },
                onCancelar = { confirmandoExclusao = false },
            )
        }
    }
}

@Composable
private fun EventMemoryRow(memory: MemoryResponse, viewModel: AppViewModel, onClick: () -> Unit, onRemover: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        MemoryCardItem(
            numero = memory.id.takeLast(4).uppercase(),
            dataHora = formatarDataHoraEvento(memory.eventAt ?: memory.createdAt),
            conteudo = memory.content,
            viewModel = viewModel,
            sensivel = memory.sensitivity == "SENSITIVE",
            onClick = onClick,
        )
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            IconSquareButton(SolarisIcon.FECHAR, "Remover do evento", onRemover)
        }
    }
}

/** Chave "dd/mm/aaaa" do dia em que a memória aconteceu (eventAt, ou createdAt na falta dele) — usada só para decidir onde quebrar o separador de dia. */
private fun diaDoOcorrido(memory: MemoryResponse): String = formatarDataHoraEvento(memory.eventAt ?: memory.createdAt).substringBefore(" · ")

private fun formatarDataHoraEvento(iso: String): String = try {
    val dt = Instant.parse(iso).toLocalDateTime(TimeZone.currentSystemDefault())
    "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}/${dt.year} · " +
        "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
} catch (e: Exception) {
    iso
}
