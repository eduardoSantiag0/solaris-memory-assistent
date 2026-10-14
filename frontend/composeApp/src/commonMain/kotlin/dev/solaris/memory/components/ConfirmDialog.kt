package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.solarisText

/** HANDOFF.md section 3 "Diálogo de confirmação": cantos retos, cabeçalho mono, título 20/600, botões lado a lado. */
@Composable
fun ConfirmDialog(
    titulo: String,
    texto: String,
    confirmarLabel: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    perigo: Boolean = true,
) {
    val colors = LocalSolarisColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.veu)
            .clickable(onClick = onCancelar),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .background(colors.fundo)
                .border(1.dp, colors.filete)
                .clickable(enabled = false) {}
                .padding(20.dp),
        ) {
            Text(titulo, style = solarisText.tituloDialogo, color = colors.texto)
            Text(texto, style = solarisText.corpo, color = colors.texto, modifier = Modifier.padding(top = 12.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                SecondaryButton(text = "Cancelar", onClick = onCancelar, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                if (perigo) {
                    DangerButton(text = confirmarLabel, onClick = onConfirmar, modifier = Modifier.weight(1f))
                } else {
                    PrimaryButton(text = confirmarLabel, onClick = onConfirmar, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
