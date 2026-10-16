package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.solarisText

/**
 * HANDOFF.md v1.1 section 3 "Cabeçalho principal" — usado só em Conversa e
 * Memórias. Grade de 3 colunas: status da VPN à esquerda, símbolo animado
 * no lugar do título (substituindo o texto `Solaris`), engrenagem à
 * direita. Quem separa do conteúdo são as abas (SectionTabs), por isso não
 * há filete inferior aqui.
 */
@Composable
fun AppHeader(onAjustes: () -> Unit, vpnOk: Boolean = true, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .background(colors.fundo)
            .padding(start = 20.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) { VpnStatus(vpnOk) }
        SolarisLogoSprite(
            modifier = Modifier.size(width = 44.dp, height = 33.dp),
            corPlaneta = colors.usuario,
            corTraco = colors.textoSuave,
        )
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            IconSquareButton(SolarisIcon.AJUSTES, "Ajustes", onAjustes)
        }
    }
}

/**
 * HANDOFF.md v1.1 section 3 "Cabeçalho secundário" — Detalhe, Ajustes,
 * Aparência. `onAjustes` nulo deixa o espaço vazio (ex.: dentro do
 * próprio Ajustes, não faz sentido levar pra ele mesmo).
 */
@Composable
fun BackHeader(titulo: String, onVoltar: () -> Unit, onAjustes: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    val fileteColor = colors.divisoria
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .background(colors.fundo)
            .drawBehind {
                drawLine(fileteColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            }
            .padding(start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconSquareButton(SolarisIcon.VOLTAR, "Voltar", onVoltar)
        Text(
            titulo,
            style = solarisText.tituloTela,
            color = colors.texto,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
        )
        if (onAjustes != null) {
            IconSquareButton(SolarisIcon.AJUSTES, "Ajustes", onAjustes)
        } else {
            Spacer(Modifier.size(44.dp))
        }
    }
}

@Composable
private fun VpnStatus(ok: Boolean) {
    val colors = LocalSolarisColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (ok) {
            Box(Modifier.size(7.dp).background(colors.status))
        } else {
            Box(Modifier.size(7.dp).border(1.dp, colors.acao))
        }
        Spacer(Modifier.width(8.dp))
        Text(if (ok) "VPN · OK" else "SEM VPN", style = solarisText.meta, color = colors.texto)
    }
}
