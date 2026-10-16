package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.solarisText

/**
 * HANDOFF.md v1.1 section 3 "Ficha" — lista de Memórias e Detalhe. Sem
 * borda (2.1: superfícies preenchidas); cabeçalho (`Nº XXXX` + selos)
 * separado do texto por um filete tracejado. A variante do chat
 * (MemoryCardFonte) foi substituída pela SourceLink, uma linha compacta —
 * ver components/SourceLink.kt.
 */
@Composable
fun MemoryCardItem(
    numero: String,
    dataHora: String,
    conteudo: String,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier,
    expirada: Boolean = false,
    sensivel: Boolean = false,
    stamps: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null,
) {
    val colors = LocalSolarisColors.current
    val tracejadoColor = colors.tracejado

    Column(
        modifier = modifier
            .fillMaxWidth()
            .let { if (expirada) it.dashedBorder(tracejadoColor) else it.background(colors.superficie) }
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(tracejadoColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                }
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            // Mesma correção do cabeçalho da tela de Detalhe (15/10): label e
            // selos numa linha só quebravam/sobrepunham quando a memória era
            // temporária (com detalhe de tempo) e sensível ao mesmo tempo.
            Text("Nº $numero", style = solarisText.meta, color = colors.texto)
            Row(modifier = Modifier.padding(top = 6.dp)) { stamps() }
        }
        if (sensivel) {
            MaskedValue(conteudo, viewModel, modifier = Modifier.padding(horizontal = 14.dp).padding(top = 12.dp, bottom = 4.dp))
        } else {
            Text(
                conteudo,
                style = solarisText.corpo,
                color = colors.texto,
                textDecoration = if (expirada) TextDecoration.LineThrough else null,
                modifier = Modifier.padding(horizontal = 14.dp).padding(top = 12.dp, bottom = 4.dp),
            )
        }
        Text(dataHora, style = solarisText.meta, color = colors.textoSecundario, modifier = Modifier.padding(horizontal = 14.dp).padding(bottom = 12.dp))
    }
}
