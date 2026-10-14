package dev.solaris.memory.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.SolarisIconGlyph
import dev.solaris.memory.theme.solarisText

/**
 * HANDOFF.md v1.1 section 3 "Linha de fonte" — substitui a Ficha-fonte
 * grande do chat v1.0. Linha compacta em mono, sublinhado (aproximação:
 * Compose não tem sublinhado pontilhado nativo), abre o Detalhe.
 */
@Composable
fun SourceLink(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    Row(
        modifier = modifier.heightIn(min = 44.dp).clickable(onClickLabel = texto, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SolarisIconGlyph(SolarisIcon.DOCUMENTO, colors.textoSuave, size = 13.dp)
        Text(texto, style = solarisText.meta, color = colors.textoSuave, textDecoration = TextDecoration.Underline)
        SolarisIconGlyph(SolarisIcon.SETA_DIREITA, colors.textoSuave, size = 12.dp)
    }
}
