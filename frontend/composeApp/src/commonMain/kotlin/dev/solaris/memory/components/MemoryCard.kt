package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.solarisText

/**
 * HANDOFF.md section 3 "Ficha": fundo `superficie`, filete 1dp, cabeçalho
 * mono, texto literal, selo no rodapé. Variante Fonte (chat) tem um "furo"
 * decorativo embaixo; variante Item (lista) não. Detalhe fica para a tela
 * de detalhe, ainda não implementada.
 */
@Composable
fun MemoryCardFonte(
    numero: String,
    dataHora: String,
    conteudo: String,
    modifier: Modifier = Modifier,
    sensivel: Boolean = false,
    stamp: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = LocalSolarisColors.current
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.superficie)
                .border(1.dp, colors.filete)
                .let { if (onClick != null) it.clickable(onClick = onClick) else it }
                .padding(12.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("FICHA Nº $numero", style = solarisText.meta, color = colors.texto)
                Text(dataHora, style = solarisText.meta, color = colors.textoSecundario)
            }
            if (sensivel) {
                MaskedValue(conteudo, modifier = Modifier.padding(top = 8.dp))
            } else {
                Text(conteudo, style = solarisText.corpo, color = colors.texto, modifier = Modifier.padding(top = 8.dp))
            }
            if (stamp != null) {
                Box(Modifier.padding(top = 8.dp)) { stamp() }
            }
        }
        // "Furo" decorativo centralizado embaixo.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 4.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(colors.fundo)
                .border(1.dp, colors.filete, CircleShape),
        )
    }
}

@Composable
fun MemoryCardItem(
    numero: String,
    dataHora: String,
    conteudo: String,
    modifier: Modifier = Modifier,
    expirada: Boolean = false,
    sensivel: Boolean = false,
    stamps: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null,
) {
    val colors = LocalSolarisColors.current
    val borderModifier = if (expirada) Modifier.dashedBorder(colors.filete) else Modifier.border(1.dp, colors.filete)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.superficie)
            .then(borderModifier)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Nº $numero", style = solarisText.meta, color = colors.texto)
            stamps()
        }
        if (sensivel) {
            MaskedValue(conteudo, modifier = Modifier.padding(top = 8.dp))
        } else {
            Text(
                conteudo,
                style = solarisText.corpo,
                color = colors.texto,
                textDecoration = if (expirada) TextDecoration.LineThrough else null,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Text(dataHora, style = solarisText.meta, color = colors.textoSecundario, modifier = Modifier.padding(top = 8.dp))
    }
}
