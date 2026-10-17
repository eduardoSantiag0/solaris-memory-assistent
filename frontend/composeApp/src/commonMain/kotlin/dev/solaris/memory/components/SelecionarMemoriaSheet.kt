package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.network.MemoryResponse
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.solarisText

/** "Adicionar memória" (Evento) — busca + toque pra associar, exclui as que já pertencem ao evento. */
@Composable
fun SelecionarMemoriaSheet(
    viewModel: AppViewModel,
    jaNoEvento: Set<String>,
    onDismiss: () -> Unit,
    onSelecionar: (MemoryResponse) -> Unit,
) {
    val colors = LocalSolarisColors.current
    var busca by remember { mutableStateOf("") }

    val disponiveis = viewModel.memories.filter { memory ->
        memory.id !in jaNoEvento && memory.status != "DELETED" &&
            (busca.isBlank() || memory.content.contains(busca, ignoreCase = true))
    }

    Box(
        modifier = Modifier.fillMaxSize().background(colors.veu).clickable(onClick = onDismiss),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .background(colors.fundo)
                .clickable(enabled = false) {}
                .padding(16.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Adicionar memória", style = solarisText.tituloFolha, color = colors.texto)
                IconSquareButton(SolarisIcon.FECHAR, "Fechar", onDismiss)
            }

            SolarisTextField(
                value = busca,
                onValueChange = { busca = it },
                placeholder = "Buscar memórias…",
                icon = SolarisIcon.BUSCAR,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
            )

            if (disponiveis.isEmpty()) {
                Text(
                    "Nenhuma memória disponível para adicionar.",
                    style = solarisText.corpo,
                    color = colors.texto,
                    modifier = Modifier.padding(top = 20.dp),
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(disponiveis) { memory ->
                    MemoryCardItem(
                        numero = memory.id.takeLast(4).uppercase(),
                        dataHora = "",
                        conteudo = memory.content,
                        viewModel = viewModel,
                        sensivel = memory.sensitivity == "SENSITIVE",
                        onClick = { onSelecionar(memory) },
                    )
                }
            }
        }
    }
}
