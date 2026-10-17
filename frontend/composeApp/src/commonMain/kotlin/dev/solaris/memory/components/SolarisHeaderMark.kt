package dev.solaris.memory.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * design/solaris-logo-cabecalho/INSTRUCOES.md (16/10/2026): substitui o texto
 * "Solaris" e o antigo [SolarisLogoSprite] no cabeçalho principal — planeta,
 * horizonte, reflexo ondulando, ondas e estrelas piscando, numa única cor
 * (`textoSuave`) sobre uma grade de pixels 32×24. Quadro congela em 0 quando
 * o sistema tem animações reduzidas/desativadas.
 */
@Composable
fun SolarisHeaderMark(modifier: Modifier = Modifier) {
    val cor = LocalSolarisColors.current.textoSuave
    val parado = animacoesReduzidas()
    val quadro by produceState(0, parado) {
        if (!parado) {
            while (true) {
                delay(160)
                value = (value + 1) % 192
            }
        }
    }

    Canvas(
        modifier
            .size(width = 48.dp, height = 36.dp)
            .semantics { contentDescription = "Solaris"; heading() },
    ) {
        val p = floor(size.width / 32f).coerceAtLeast(1f)
        val ox = ((size.width - 32 * p) / 2f).roundToInt().toFloat()
        val oy = ((size.height - 24 * p) / 2f).roundToInt().toFloat()
        val i = quadro

        fun r(x: Int, y: Int, w: Int, a: Float) {
            for (xx in intArrayOf(x, x - 32, x + 32)) {
                val x0 = xx.coerceAtLeast(0)
                val x1 = (xx + w).coerceAtMost(32)
                if (x1 > x0) {
                    drawRect(
                        cor.copy(alpha = a),
                        Offset(ox + x0 * p, oy + y * p),
                        Size((x1 - x0) * p, p),
                    )
                }
            }
        }

        // Estrelas (70%) — some quando (i + 3k) % 6 == 0.
        listOf(4 to 3, 27 to 5, 7 to 9, 24 to 2, 29 to 10).forEachIndexed { k, (x, y) ->
            if ((i + k * 3) % 6 != 0) r(x, y, 1, .7f)
        }

        // Planeta (100%) — fixo.
        listOf(
            Triple(3, 14, 4), Triple(4, 12, 8), Triple(5, 11, 10), Triple(6, 11, 10),
            Triple(7, 11, 10), Triple(8, 11, 10), Triple(9, 11, 10), Triple(10, 12, 8),
            Triple(11, 14, 4),
        ).forEach { (y, x, w) -> r(x, y, w, 1f) }

        // Horizonte (100%) — fixo.
        r(0, 14, 32, 1f)

        // Reflexo (55%) — dx = round(sin(i/12 · 2π + 1,6r)).
        listOf(Triple(16, 11, 10), Triple(18, 12, 8), Triple(20, 14, 4), Triple(22, 15, 2))
            .forEachIndexed { k, (y, x, w) ->
                val dx = round(sin(i / 12f * 2f * PI.toFloat() + k * 1.6f)).toInt()
                r(x + dx, y, w, .55f)
            }

        // Ondas (35%) — x = (x0 + direção · (i / 2)) mod 32, dando a volta na borda.
        listOf(listOf(16, 3, 3, 1), listOf(18, 24, 4, -1), listOf(21, 6, 3, 1), listOf(22, 25, 2, -1))
            .forEach { (y, x0, w, d) -> r(Math.floorMod(x0 + d * (i / 2), 32), y, w, .35f) }
    }
}
