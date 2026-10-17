package dev.solaris.memory.components

import androidx.compose.foundation.background
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

enum class StampType { PERMANENTE, TEMPORARIA, SENSIVEL, VENCIDA, ANEXO }

/**
 * HANDOFF.md: "Sempre ícone + texto, nunca só cor" (acessibilidade, seção
 * 11.5 dos requisitos). v1.1 2.1: selos são etiquetas preenchidas, sem
 * borda — exceto VENCIDA, que fica transparente com contorno tracejado.
 */
@Composable
fun Stamp(type: StampType, detail: String? = null, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    val (icon, label, tint, background) = when (type) {
        StampType.PERMANENTE -> Quadruple(SolarisIcon.PERMANENTE, "PERMANENTE", colors.texto, colors.seloPermanente)
        StampType.TEMPORARIA -> Quadruple(SolarisIcon.TEMPORARIA, "TEMPORÁRIA", colors.acao, colors.seloTemporaria)
        StampType.SENSIVEL -> Quadruple(SolarisIcon.SENSIVEL, "SENSÍVEL", colors.acao, colors.seloTemporaria)
        StampType.VENCIDA -> Quadruple(null, "VENCIDA", colors.tracejado, null)
        StampType.ANEXO -> Quadruple(SolarisIcon.ANEXO, "ANEXO", colors.texto, colors.seloPermanente)
    }
    val text = if (detail != null) "$label · $detail" else label
    val fundoModifier = if (background != null) Modifier.background(background) else Modifier.dashedBorder(colors.tracejado)

    Row(
        modifier = modifier.then(fundoModifier).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            SolarisIconGlyph(icon, tint, size = 14.dp)
            Spacer(Modifier.width(4.dp))
        }
        Text(text, style = solarisText.meta, color = colors.texto)
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
