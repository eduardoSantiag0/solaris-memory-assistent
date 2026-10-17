package dev.solaris.memory.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.components.IconSquareButton
import dev.solaris.memory.components.NovoEventoDialog
import dev.solaris.memory.network.EventResponse
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.solarisText
import kotlinx.coroutines.launch

/** Lista de eventos — agrupamentos manuais de memórias, ordenados pelos mais recentes. */
@Composable
fun EventsScreen(viewModel: AppViewModel, onAbrirDetalhe: (String) -> Unit) {
    val colors = LocalSolarisColors.current
    var mostrarNovoEvento by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.refreshEvents()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(colors.fundo)) {
            val divisoriaColor = colors.divisoria
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(divisoriaColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                    }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Eventos", style = solarisText.tituloTela, color = colors.texto)
                IconSquareButton(SolarisIcon.NOVA, "Novo evento", { mostrarNovoEvento = true }, preenchido = true)
            }

            if (viewModel.events.isEmpty()) {
                Text(
                    "Nenhum evento ainda. Agrupe memórias relacionadas criando um.",
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
                items(viewModel.events) { evento -> EventRow(evento, onClick = { onAbrirDetalhe(evento.id) }) }
            }
        }

        if (mostrarNovoEvento) {
            NovoEventoDialog(
                onDismiss = { mostrarNovoEvento = false },
                onSalvar = { titulo, descricao ->
                    scope.launch {
                        erro = viewModel.createEvent(titulo, descricao)
                        if (erro == null) mostrarNovoEvento = false
                    }
                },
            )
        }
    }
}

@Composable
private fun EventRow(evento: EventResponse, onClick: () -> Unit) {
    val colors = LocalSolarisColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.superficie)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(evento.title, style = solarisText.corpo.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = colors.texto)
        evento.description?.let {
            Text(it, style = solarisText.corpo, color = colors.textoSecundario, modifier = Modifier.padding(top = 4.dp))
        }
        Text(
            "${evento.memoryCount} memória${if (evento.memoryCount == 1L) "" else "s"}",
            style = solarisText.meta,
            color = colors.textoSuave,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
