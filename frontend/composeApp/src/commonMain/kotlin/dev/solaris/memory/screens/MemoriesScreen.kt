package dev.solaris.memory.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.components.IconSquareButton
import dev.solaris.memory.components.MemoryCardItem
import dev.solaris.memory.components.NovaMemoriaSheet
import dev.solaris.memory.components.SolarisTextField
import dev.solaris.memory.components.Stamp
import dev.solaris.memory.components.StampType
import dev.solaris.memory.network.MemoryResponse
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.solarisText
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private enum class TipoFiltro { TODAS, PERMANENTES, TEMPORARIAS }

@Composable
fun MemoriesScreen(viewModel: AppViewModel, onAbrirDetalhe: (String) -> Unit) {
    val colors = LocalSolarisColors.current
    var busca by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf(TipoFiltro.TODAS) }
    var mostrarNovaMemoria by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshMemories()
    }

    val filtradas = viewModel.memories.filter { memory ->
        val matchesBusca = busca.isBlank() || memory.content.contains(busca, ignoreCase = true)
        val matchesTipo = when (filtro) {
            TipoFiltro.TODAS -> true
            TipoFiltro.PERMANENTES -> memory.expiresAt == null
            TipoFiltro.TEMPORARIAS -> memory.expiresAt != null
        }
        matchesBusca && matchesTipo
    }
    val vigentes = filtradas.filter { it.status != "EXPIRED" && it.status != "DELETED" }
    val vencidas = filtradas.filter { it.status == "EXPIRED" }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(colors.fundo)) {
            val divisoriaColor = colors.divisoria
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(divisoriaColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                    }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SolarisTextField(
                        value = busca,
                        onValueChange = { busca = it },
                        placeholder = "Buscar memórias…",
                        icon = SolarisIcon.BUSCAR,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(10.dp))
                    IconSquareButton(SolarisIcon.NOVA, "Nova memória", { mostrarNovaMemoria = true }, preenchido = true)
                }

                Row(modifier = Modifier.padding(top = 12.dp)) {
                    FiltroTab("TODAS", filtro == TipoFiltro.TODAS) { filtro = TipoFiltro.TODAS }
                    Spacer(Modifier.width(20.dp))
                    FiltroTab("PERMANENTES", filtro == TipoFiltro.PERMANENTES) { filtro = TipoFiltro.PERMANENTES }
                    Spacer(Modifier.width(20.dp))
                    FiltroTab("TEMPORÁRIAS", filtro == TipoFiltro.TEMPORARIAS) { filtro = TipoFiltro.TEMPORARIAS }
                }
            }

            if (viewModel.memories.isEmpty()) {
                Text(
                    "Não há nenhuma ficha sobre isso. Quer guardar agora?",
                    color = colors.texto,
                    style = solarisText.corpo,
                    modifier = Modifier.padding(20.dp),
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 20.dp),
            ) {
                if (vigentes.isNotEmpty()) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("VIGENTES · ${vigentes.size}", style = solarisText.meta, color = colors.texto)
                            Text("MAIS RECENTES", style = solarisText.meta, color = colors.texto)
                        }
                    }
                    // Separador por dia — modo "linha do tempo" leve pra escanear
                    // uma lista grande sem abrir cada ficha (achado da pesquisa de
                    // UI 16/10), complementar ao "VIGENTES · N" já existente.
                    itemsIndexed(vigentes) { index, memory ->
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            val anterior = vigentes.getOrNull(index - 1)
                            val diaAtual = diaDaMemoria(memory)
                            if (anterior == null || diaDaMemoria(anterior) != diaAtual) {
                                Text(
                                    "— $diaAtual —",
                                    style = solarisText.meta,
                                    color = colors.textoSuave,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            }
                            MemoryRow(memory, viewModel, onAbrirDetalhe)
                        }
                    }
                }
                if (vencidas.isNotEmpty()) {
                    item {
                        Text(
                            "VENCIDAS · ${vencidas.size} · FORA DAS RESPOSTAS",
                            style = solarisText.meta,
                            color = colors.textoSuave,
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    drawLine(divisoriaColor, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                                }
                                .padding(top = 14.dp),
                        )
                    }
                    items(vencidas) { memory -> MemoryRow(memory, viewModel, onAbrirDetalhe, expirada = true) }
                }
            }
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

@Composable
private fun FiltroTab(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = LocalSolarisColors.current
    val indicador = colors.foco
    Text(
        label,
        style = if (active) solarisText.navegacaoAtiva else solarisText.navegacao,
        color = if (active) colors.texto else colors.textoSuave,
        modifier = Modifier
            .clickable(onClick = onClick)
            .drawBehind {
                if (active) {
                    drawLine(indicador, Offset(0f, size.height + 4.dp.toPx()), Offset(size.width, size.height + 4.dp.toPx()), strokeWidth = 2.dp.toPx())
                }
            }
            .padding(bottom = 6.dp),
    )
}

@Composable
private fun MemoryRow(memory: MemoryResponse, viewModel: AppViewModel, onAbrirDetalhe: (String) -> Unit, expirada: Boolean = false) {
    val numero = memory.id.takeLast(4).uppercase()
    val sensivel = memory.sensitivity == "SENSITIVE"
    MemoryCardItem(
        numero = numero,
        dataHora = formatarDataHora(memory.createdAt),
        conteudo = memory.content,
        viewModel = viewModel,
        expirada = expirada,
        sensivel = sensivel,
        stamps = {
            Row {
                if (expirada) {
                    Stamp(StampType.VENCIDA, detail = formatarData(memory.expiresAt))
                } else if (memory.expiresAt != null) {
                    Stamp(StampType.TEMPORARIA, detail = remainingLabel(memory.expiresAt))
                } else {
                    Stamp(StampType.PERMANENTE)
                }
                if (sensivel) {
                    Spacer(Modifier.width(6.dp))
                    Stamp(StampType.SENSIVEL)
                }
                if (memory.hasAttachment) {
                    Spacer(Modifier.width(6.dp))
                    Stamp(StampType.ANEXO)
                }
            }
        },
        onClick = { onAbrirDetalhe(memory.id) },
    )
}

/** "dd/mm/aaaa" só pra decidir onde quebrar o separador de dia — mesma base de dados do card (createdAt), não um campo novo. */
private fun diaDaMemoria(memory: MemoryResponse): String = formatarDataHora(memory.createdAt).substringBefore(" · ")

private fun formatarDataHora(iso: String?): String {
    if (iso == null) return ""
    return try {
        val dt = Instant.parse(iso).toLocalDateTime(TimeZone.currentSystemDefault())
        "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}/${dt.year} · " +
            "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        iso
    }
}

private fun formatarData(iso: String?): String {
    if (iso == null) return ""
    return try {
        val dt = Instant.parse(iso).toLocalDateTime(TimeZone.currentSystemDefault())
        "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        iso
    }
}

private fun remainingLabel(expiresAt: String?): String {
    if (expiresAt == null) return ""
    return try {
        val expiry = Instant.parse(expiresAt)
        val now = Clock.System.now()
        val remaining = expiry - now
        when {
            remaining.isNegative() -> "0H"
            remaining.inWholeHours < 1 -> "${remaining.inWholeMinutes}M"
            remaining.inWholeDays < 1 -> "${remaining.inWholeHours}H"
            else -> "${remaining.inWholeDays}D ${remaining.inWholeHours % 24}H"
        }
    } catch (e: Exception) {
        ""
    }
}
