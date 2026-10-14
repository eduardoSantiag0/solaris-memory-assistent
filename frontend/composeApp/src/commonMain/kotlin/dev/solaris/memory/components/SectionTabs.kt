package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.solarisText

enum class SecaoPrincipal { CONVERSA, MEMORIAS }

/**
 * HANDOFF.md v1.1 section 3 "Abas" — substitui a navegação inferior (não
 * há mais barra de 3 itens). Só Conversa e Memórias; Ajustes é alcançado
 * pela engrenagem do AppHeader, não é uma aba.
 */
@Composable
fun SectionTabs(selecionada: SecaoPrincipal, contadorMemorias: Int, onSelecionar: (SecaoPrincipal) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    val fileteColor = colors.divisoria
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(colors.fundo)
            .drawBehind {
                drawLine(fileteColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            }
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionTab("Conversa", null, selecionada == SecaoPrincipal.CONVERSA) { onSelecionar(SecaoPrincipal.CONVERSA) }
        SectionTab("Memórias", contadorMemorias, selecionada == SecaoPrincipal.MEMORIAS) { onSelecionar(SecaoPrincipal.MEMORIAS) }
    }
}

@Composable
private fun SectionTab(label: String, contador: Int?, ativa: Boolean, onClick: () -> Unit) {
    val colors = LocalSolarisColors.current
    val indicador = colors.foco
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClickLabel = label, onClick = onClick)
            .drawBehind {
                if (ativa) {
                    drawLine(indicador, Offset(0f, size.height - 1.dp.toPx()), Offset(size.width, size.height - 1.dp.toPx()), strokeWidth = 2.dp.toPx())
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            label,
            style = solarisText.corpo.copy(fontSize = 15.sp, fontWeight = if (ativa) FontWeight.SemiBold else FontWeight.Normal),
            color = if (ativa) colors.texto else colors.textoSuave,
        )
        if (contador != null) {
            Text(
                "$contador",
                style = solarisText.meta,
                color = colors.texto,
                modifier = Modifier.background(colors.preenchimento).padding(horizontal = 6.dp, vertical = 1.dp),
            )
        }
    }
}
