package dev.solaris.memory.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// HANDOFF.md 1: símbolo "Reflexo A2" — existe só em versão escura, sempre
// sobre Deep Ocean, independente da paleta/tema ativos (assets/logo-simbolo.svg).
private val DEEP_OCEAN = Color(0xFF172621)
private val LOGO_GOLD = Color(0xFFB8A47B)
private val LOGO_PAPER = Color(0xFFE8E5D8)

/** Square tile with the logo, for the header (design-tokens.json: 30dp in the header). */
@Composable
fun SolarisLogoTile(size: Dp = 30.dp, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(modifier = modifier.size(size).background(DEEP_OCEAN)) {
        SolarisLogoGlyph(size = size)
    }
}

/** Just the glyph (planet + horizon + reflection), no background square — for places already on Deep Ocean (splash screen). */
@Composable
fun SolarisLogoGlyph(size: Dp = 30.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val k = this.size.width / 64f
        fun off(x: Float, y: Float) = Offset(x * k, y * k)

        drawCircle(LOGO_GOLD, radius = 12f * k, center = off(32f, 24f))
        drawLine(LOGO_PAPER, off(8f, 40f), off(56f, 40f), strokeWidth = 2.5f * k, cap = StrokeCap.Square)
        drawRect(LOGO_GOLD.copy(alpha = 0.6f), topLeft = off(21f, 45f), size = androidx.compose.ui.geometry.Size(22f * k, 3f * k))
        drawRect(LOGO_GOLD.copy(alpha = 0.6f), topLeft = off(27f, 51f), size = androidx.compose.ui.geometry.Size(10f * k, 3f * k))
    }
}
