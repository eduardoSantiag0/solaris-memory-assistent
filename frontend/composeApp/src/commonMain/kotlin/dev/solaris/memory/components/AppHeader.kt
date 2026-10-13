package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.solarisText

/** HANDOFF.md section 3: Cabeçalho — altura mínima 56dp, filete inferior, logo ou voltar à esquerda, status VPN à direita. */
@Composable
fun AppHeader(
    titulo: String,
    contador: Int? = null,
    onVoltar: (() -> Unit)? = null,
    vpnOk: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSolarisColors.current
    val fileteColor = colors.filete
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .background(colors.fundo)
            .drawBehind {
                drawLine(fileteColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onVoltar != null) {
            IconSquareButton(SolarisIcon.VOLTAR, "Voltar", onVoltar)
        } else {
            SolarisLogoTile(size = 30.dp)
        }
        Spacer(Modifier.width(12.dp))
        val tituloCompleto = if (contador != null) "$titulo · $contador" else titulo
        Text(tituloCompleto, style = solarisText.tituloTela, color = colors.texto, modifier = Modifier.weight(1f))

        VpnStatus(vpnOk)
    }
}

@Composable
private fun VpnStatus(ok: Boolean) {
    val colors = LocalSolarisColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (ok) {
            Box(Modifier.size(8.dp).background(colors.status))
        } else {
            Box(Modifier.size(8.dp).border(1.dp, colors.acao))
        }
        Spacer(Modifier.width(6.dp))
        Text(if (ok) "VPN · OK" else "SEM VPN", style = solarisText.meta, color = colors.texto)
    }
}
