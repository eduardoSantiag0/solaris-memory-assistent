package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.SolarisIconGlyph
import dev.solaris.memory.theme.solarisText

enum class NavTab(val label: String, val icon: SolarisIcon) {
    CHAT("CHAT", SolarisIcon.CHAT),
    MEMORIAS("MEMÓRIAS", SolarisIcon.MEMORIAS),
    AJUSTES("AJUSTES", SolarisIcon.AJUSTES),
}

/** HANDOFF.md section 3: 64dp, filete superior de 2dp em `foco` no item ativo, nunca indicador em pílula. */
@Composable
fun BottomNav(selected: NavTab, onSelect: (NavTab) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    val fileteColor = colors.filete
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .background(colors.fundo)
            .drawBehind {
                drawLine(fileteColor, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
            },
    ) {
        for (tab in NavTab.entries) {
            val active = tab == selected
            val indicatorColor = colors.foco
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClickLabel = tab.label) { onSelect(tab) }
                    .drawBehind {
                        if (active) {
                            drawLine(
                                color = indicatorColor,
                                start = androidx.compose.ui.geometry.Offset(0f, 1f),
                                end = androidx.compose.ui.geometry.Offset(size.width, 1f),
                                strokeWidth = 2.dp.toPx(),
                            )
                        }
                    }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SolarisIconGlyph(tab.icon, if (active) colors.texto else colors.textoSecundario)
                Text(
                    tab.label,
                    style = if (active) solarisText.navegacaoAtiva else solarisText.navegacao,
                    color = if (active) colors.texto else colors.textoSecundario,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
