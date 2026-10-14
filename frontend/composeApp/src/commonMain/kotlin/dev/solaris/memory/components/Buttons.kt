package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.SolarisIconGlyph
import dev.solaris.memory.theme.solarisText

/** design-tokens.json dimensoes.botaoAltura (48) / alvoToqueMinimo (44). */
private val BUTTON_HEIGHT = 48.dp
private val MIN_TOUCH_TARGET = 44.dp

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = LocalSolarisColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BUTTON_HEIGHT)
            .background(if (enabled) colors.texto else colors.trilho)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(text, style = solarisText.botao, color = colors.fundo)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = LocalSolarisColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BUTTON_HEIGHT)
            .border(1.dp, colors.filete)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(text, style = solarisText.botao, color = colors.texto)
    }
}

@Composable
fun DangerButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = LocalSolarisColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BUTTON_HEIGHT)
            .background(colors.acao)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(text, style = solarisText.botao, color = colors.textoSobreAcao)
    }
}

/** Dashed-border style areas (ex: "Importar estilo") use this with a solid border as a stand-in — dashed Compose borders need a custom DashPathEffect, applied at the call site's Modifier when needed. */
@Composable
fun OutlineChip(text: String, onClick: () -> Unit, icon: SolarisIcon? = null, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    Box(
        modifier = modifier
            .border(1.dp, colors.filete)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                SolarisIconGlyph(icon, colors.texto, size = 18.dp)
                androidx.compose.foundation.layout.Spacer(Modifier.widthIn(min = 8.dp))
            }
            androidx.compose.material3.Text(text, style = solarisText.botao, color = colors.texto, textAlign = TextAlign.Center)
        }
    }
}

/**
 * HANDOFF.md 2.1: icon-only buttons are transparent by default (voltar,
 * fechar, a engrenagem do cabeçalho) — `preenchido = true` gives the
 * filled-`preenchimento`, no-border treatment used for "+" e os passos
 * do stepper de dias.
 */
@Composable
fun IconSquareButton(icon: SolarisIcon, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color? = null, preenchido: Boolean = false) {
    val colors = LocalSolarisColors.current
    Box(
        modifier = modifier
            .size(MIN_TOUCH_TARGET)
            .let { if (preenchido) it.background(colors.preenchimento) else it }
            .clickable(onClick = onClick, onClickLabel = contentDescription),
        contentAlignment = Alignment.Center,
    ) {
        SolarisIconGlyph(icon, tint ?: colors.texto)
    }
}
