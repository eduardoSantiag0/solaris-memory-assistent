package dev.solaris.memory.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.solaris.memory.components.SolarisLogoSprite
import dev.solaris.memory.theme.plexMono
import dev.solaris.memory.theme.plexSans

// HANDOFF.md tela 01: fundo Deep Ocean, independente da paleta/tema ativos — igual ao símbolo (components/SolarisLogo.kt).
private val DEEP_OCEAN = Color(0xFF172621)
private val LOGO_GOLD = Color(0xFFB8A47B)
private val LOGO_PAPER = Color(0xFFE8E5D8)
private val TRILHO_ABERTURA = Color(0xFF566C5B)

/**
 * HANDOFF.md tela 01 "Abertura" — mostrada enquanto o app autentica e
 * confere `/api/v1/health` (AppViewModel.isRestoringSession). A animação do
 * `reflexo.gif` é reproduzida como um sprite no Canvas, não o GIF em si
 * ("prefira um sprite em Canvas do Compose... em vez do GIF" — seção 4).
 */
@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DEEP_OCEAN)
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SolarisLogoSprite(
                modifier = Modifier.size(width = 200.dp, height = 150.dp),
                corPlaneta = LOGO_GOLD,
                corTraco = LOGO_PAPER,
            )
            Spacer(Modifier.height(28.dp))
            Text(
                "solaris",
                style = TextStyle(fontFamily = plexMono(), fontWeight = FontWeight.Medium, fontSize = 32.sp, letterSpacing = 2.sp),
                color = LOGO_PAPER,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "MEMORY-ASSISTENT",
                style = TextStyle(fontFamily = plexMono(), fontWeight = FontWeight.Normal, fontSize = 12.sp, letterSpacing = 3.sp),
                color = LOGO_GOLD,
            )
            Spacer(Modifier.height(28.dp))
            Text(
                "Lembra só o que você registrou.",
                style = TextStyle(fontFamily = plexSans(), fontSize = 16.sp, lineHeight = 24.sp),
                color = LOGO_PAPER,
                modifier = Modifier.widthIn(max = 260.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            BarraBuscando()
            Text(
                "CONECTANDO AO SERVIDOR PRIVADO",
                style = TextStyle(fontFamily = plexMono(), fontSize = 12.sp, letterSpacing = 1.sp),
                color = LOGO_PAPER,
            )
            Text(
                "via WireGuard · nada sai do seu servidor",
                style = TextStyle(fontFamily = plexMono(), fontSize = 12.sp),
                color = LOGO_GOLD,
            )
        }
    }
}

@Composable
private fun BarraBuscando() {
    val transicao = rememberInfiniteTransition(label = "barraBuscando")
    val posicao by transicao.animateFloat(
        initialValue = 0f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(animation = tween(1600, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "posicao",
    )
    Canvas(modifier = Modifier.width(160.dp).height(1.dp)) {
        drawLine(TRILHO_ABERTURA, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
        val segmento = size.width * 0.3f
        val x = size.width * posicao
        drawLine(LOGO_GOLD, Offset(x, 0f), Offset((x + segmento).coerceAtMost(size.width), 0f), strokeWidth = 1.dp.toPx())
    }
}
