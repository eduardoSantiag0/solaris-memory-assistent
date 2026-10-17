package dev.solaris.memory.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

/**
 * Planeta + horizonte + reflexo ondulando + duas estrelas — sprite animado em
 * Canvas que substitui o `assets/reflexo.gif` do handoff ("prefira um sprite
 * em Canvas do Compose... em vez do GIF"). Compartilhado entre a Abertura
 * (SplashScreen, cores fixas Deep Ocean) e o cabeçalho (AppHeader, cores do
 * tema ativo) para não duplicar a animação. `amplitude` escala o quanto o
 * reflexo se desloca a cada ciclo — como o deslocamento final é `amplitude *
 * k` (k = largura do ícone / 256), o ícone pequeno do cabeçalho precisa de
 * uma amplitude maior que o padrão pra o movimento continuar visível.
 */
@Composable
fun SolarisLogoSprite(modifier: Modifier = Modifier, corPlaneta: Color, corTraco: Color, amplitude: Float = 10f) {
    val transicao = rememberInfiniteTransition(label = "reflexo")
    val onda by transicao.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1800, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "onda",
    )
    Canvas(modifier = modifier) {
        val k = this.size.width / 256f
        fun off(x: Float, y: Float) = Offset(x * k, y * k)

        // Correção 17/10: o planeta ficava em x=100 de 256 (fora do centro),
        // deixando o símbolo visivelmente puxado pra esquerda na Abertura —
        // recentralizado pra x=128 (meio exato da largura), preservando a
        // composição relativa das estrelas e do reflexo.
        drawCircle(corPlaneta, radius = 24f * k, center = off(128f, 48f))
        drawRect(corTraco, topLeft = off(216f, 20f), size = Size(6f * k, 6f * k))
        drawRect(corTraco, topLeft = off(234f, 38f), size = Size(5f * k, 5f * k))
        drawLine(corTraco, off(0f, 80f), off(256f, 80f), strokeWidth = 2.5f * k, cap = StrokeCap.Square)

        val deslocamento = (amplitude * onda) * k
        drawRect(corPlaneta.copy(alpha = 0.7f), topLeft = off(60f + deslocamento, 92f), size = Size(90f * k, 8f * k))
        drawRect(corPlaneta.copy(alpha = 0.55f), topLeft = off(106f - deslocamento, 108f), size = Size(48f * k, 6f * k))
        drawRect(corPlaneta.copy(alpha = 0.4f), topLeft = off(86f + deslocamento, 122f), size = Size(70f * k, 6f * k))
        drawRect(corPlaneta.copy(alpha = 0.3f), topLeft = off(118f - deslocamento, 136f), size = Size(40f * k, 6f * k))
    }
}
