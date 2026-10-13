package dev.solaris.memory.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
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
import dev.solaris.memory.components.MemoryCardItem
import dev.solaris.memory.components.SolarisTextField
import dev.solaris.memory.components.Stamp
import dev.solaris.memory.components.StampType
import dev.solaris.memory.network.MemoryResponse
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.solarisText
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private enum class TipoFiltro { TODAS, PERMANENTES, TEMPORARIAS }

@Composable
fun MemoriesScreen(viewModel: AppViewModel) {
    val colors = LocalSolarisColors.current
    val scope = rememberCoroutineScope()
    var busca by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf(TipoFiltro.TODAS) }

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

    Column(modifier = Modifier.fillMaxSize().background(colors.fundo).padding(16.dp)) {
        SolarisTextField(
            value = busca,
            onValueChange = { busca = it },
            placeholder = "Buscar: chave, RG, psicólogo...",
            icon = SolarisIcon.BUSCAR,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(modifier = Modifier.padding(top = 16.dp)) {
            FiltroTab("TODAS", filtro == TipoFiltro.TODAS) { filtro = TipoFiltro.TODAS }
            Spacer(Modifier.width(20.dp))
            FiltroTab("PERMANENTES", filtro == TipoFiltro.PERMANENTES) { filtro = TipoFiltro.PERMANENTES }
            Spacer(Modifier.width(20.dp))
            FiltroTab("TEMPORÁRIAS", filtro == TipoFiltro.TEMPORARIAS) { filtro = TipoFiltro.TEMPORARIAS }
        }

        if (viewModel.memories.isEmpty()) {
            Text(
                "Não há nenhuma ficha sobre isso. Quer guardar agora?",
                color = colors.texto,
                style = solarisText.corpo,
                modifier = Modifier.padding(top = 24.dp),
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (vigentes.isNotEmpty()) {
                item { Text("VIGENTES · ${vigentes.size}", style = solarisText.meta, color = colors.textoSecundario) }
                items(vigentes) { memory -> MemoryRow(memory, scope, viewModel) }
            }
            if (vencidas.isNotEmpty()) {
                item {
                    Text(
                        "VENCIDAS · ${vencidas.size} · FORA DAS RESPOSTAS",
                        style = solarisText.meta,
                        color = colors.textoSecundario,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(vencidas) { memory -> MemoryRow(memory, scope, viewModel, expirada = true) }
            }
        }
    }
}

@Composable
private fun FiltroTab(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = LocalSolarisColors.current
    Text(
        label,
        style = if (active) solarisText.navegacaoAtiva else solarisText.navegacao,
        color = if (active) colors.texto else colors.textoSecundario,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun MemoryRow(memory: MemoryResponse, scope: kotlinx.coroutines.CoroutineScope, viewModel: AppViewModel, expirada: Boolean = false) {
    val numero = memory.id.takeLast(4).uppercase()
    MemoryCardItem(
        numero = numero,
        dataHora = formatarDataHora(memory.createdAt),
        conteudo = memory.content,
        expirada = expirada,
        stamps = {
            Row {
                if (expirada) {
                    Stamp(StampType.VENCIDA, detail = formatarData(memory.expiresAt))
                } else if (memory.expiresAt != null) {
                    Stamp(StampType.TEMPORARIA, detail = remainingLabel(memory.expiresAt))
                } else {
                    Stamp(StampType.PERMANENTE)
                }
            }
        },
        onClick = { scope.launch { /* TODO: tela de Detalhe (06) */ } },
    )
}

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
