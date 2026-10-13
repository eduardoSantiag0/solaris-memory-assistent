package dev.solaris.memory.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.SolarisIconGlyph
import dev.solaris.memory.theme.solarisText

enum class StampType { PERMANENTE, TEMPORARIA, SENSIVEL, VENCIDA }

/** HANDOFF.md: "Sempre ícone + texto, nunca só cor" (acessibilidade, seção 11.5 dos requisitos). */
@Composable
fun Stamp(type: StampType, detail: String? = null, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    val (icon, label, borderColor) = when (type) {
        StampType.PERMANENTE -> Triple(SolarisIcon.PERMANENTE, "PERMANENTE", colors.filete)
        StampType.TEMPORARIA -> Triple(SolarisIcon.TEMPORARIA, "TEMPORÁRIA", colors.acao)
        StampType.SENSIVEL -> Triple(SolarisIcon.SENSIVEL, "SENSÍVEL", colors.acao)
        StampType.VENCIDA -> Triple(null, "VENCIDA", colors.filete)
    }
    val text = if (detail != null) "$label · $detail" else label
    val borderModifier = if (type == StampType.VENCIDA) {
        Modifier.dashedBorder(borderColor)
    } else {
        Modifier.border(1.dp, borderColor)
    }

    Row(
        modifier = modifier.then(borderModifier).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            SolarisIconGlyph(icon, borderColor, size = 14.dp)
            Spacer(Modifier.width(4.dp))
        }
        Text(text, style = solarisText.meta, color = colors.texto)
    }
}
