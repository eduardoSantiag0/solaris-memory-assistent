package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.solarisText

/**
 * Create/edit an Event — título obrigatório, descrição opcional (RF:
 * "Cada evento deve possuir título e descrição opcional"). Same sheet
 * serves both, like NovaMemoriaSheet does for memories.
 */
@Composable
fun NovoEventoDialog(
    tituloInicial: String = "",
    descricaoInicial: String = "",
    editando: Boolean = false,
    onDismiss: () -> Unit,
    onSalvar: (titulo: String, descricao: String?) -> Unit,
) {
    val colors = LocalSolarisColors.current
    var titulo by remember { mutableStateOf(tituloInicial) }
    var descricao by remember { mutableStateOf(descricaoInicial) }

    Box(
        modifier = Modifier.fillMaxSize().background(colors.veu).clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .background(colors.fundo)
                .border(1.dp, colors.filete)
                .clickable(enabled = false) {}
                .padding(20.dp),
        ) {
            Text(if (editando) "Editar evento" else "Novo evento", style = solarisText.tituloDialogo, color = colors.texto)

            Text("TÍTULO", style = solarisText.meta, color = colors.texto, modifier = Modifier.padding(top = 16.dp, bottom = 6.dp))
            SolarisTextField(value = titulo, onValueChange = { titulo = it }, placeholder = "Ex.: Viagem a Floripa", modifier = Modifier.fillMaxWidth())

            Text("DESCRIÇÃO (OPCIONAL)", style = solarisText.meta, color = colors.texto, modifier = Modifier.padding(top = 16.dp, bottom = 6.dp))
            SolarisTextField(value = descricao, onValueChange = { descricao = it }, placeholder = "Ex.: Férias de janeiro", modifier = Modifier.fillMaxWidth())

            Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                SecondaryButton(text = "Cancelar", onClick = onDismiss, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                PrimaryButton(
                    text = "Salvar",
                    enabled = titulo.isNotBlank(),
                    onClick = { onSalvar(titulo.trim(), descricao.trim().ifBlank { null }) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
