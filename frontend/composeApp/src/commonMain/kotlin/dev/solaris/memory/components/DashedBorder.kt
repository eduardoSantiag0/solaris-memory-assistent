package dev.solaris.memory.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Used for "vencida" memories and the "estilos importados" placeholder — a plain `border()` only draws solid lines. */
fun Modifier.dashedBorder(color: Color, width: Dp = 1.dp, dashLength: Dp = 4.dp, gapLength: Dp = 3.dp): Modifier = drawBehind {
    val strokeWidthPx = width.toPx()
    val dashPx = dashLength.toPx()
    val gapPx = gapLength.toPx()
    drawRect(
        color = color,
        size = Size(size.width, size.height),
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f),
        ),
    )
}
