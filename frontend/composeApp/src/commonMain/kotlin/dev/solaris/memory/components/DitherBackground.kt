package dev.solaris.memory.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * HANDOFF.md 2.3: dither 2×2 discreto atrás da área rolável da conversa —
 * desenhado uma vez no contêiner (drawWithCache + onDrawBehind), não nos
 * itens da LazyColumn, então não "anda" nem cintila ao rolar. A célula é
 * arredondada para pixels inteiros antes de montar o tile, porque uma
 * célula fracionária gera moiré em densidades não inteiras.
 */
fun Modifier.ditherConversa(cor: Color): Modifier = drawWithCache {
    val c = 2.dp.toPx().roundToInt().coerceAtLeast(1)
    val bitmap = ImageBitmap(c * 2, c * 2)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply { color = cor }
    canvas.drawRect(c.toFloat(), 0f, 2f * c, c.toFloat(), paint)
    canvas.drawRect(0f, c.toFloat(), c.toFloat(), 2f * c, paint)
    val pincel = ShaderBrush(ImageShader(bitmap, TileMode.Repeated, TileMode.Repeated))
    onDrawBehind { drawRect(pincel) }
}
