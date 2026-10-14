package dev.solaris.memory.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.solarisText

/** HANDOFF.md 02-chat: bloco à direita, fundo `usuario`, largura máx. 80%, comandos em mono 600. */
@Composable
fun UserMessage(text: String, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(colors.usuario)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            val isCommand = text.startsWith("/add")
            if (isCommand) {
                val spaceIndex = text.indexOf(' ').let { if (it == -1) text.length else it }
                val command = text.take(spaceIndex)
                val rest = text.drop(spaceIndex)
                Text(
                    buildAnnotatedString {
                        withStyle(solarisText.comando.toSpanStyle()) { append(command) }
                        append(rest)
                    },
                    style = solarisText.corpo,
                    color = colors.textoSobreUsuario,
                )
            } else {
                Text(text, style = solarisText.corpo, color = colors.textoSobreUsuario)
            }
        }
    }
}

/** HANDOFF.md 02-chat: "Sem balão." Meta `SOLARIS · HH:mm` acima de texto corrido. */
@Composable
fun AssistantMessageMeta(horario: String, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    Text("SOLARIS · $horario", style = solarisText.meta, color = colors.textoSecundario, modifier = modifier)
}

@Composable
fun AssistantMessageBody(text: String, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    Text(text, style = solarisText.corpo, color = colors.texto, modifier = modifier)
}

/**
 * HANDOFF.md v1.1 section 3 "Confirmação de gravação" — resposta de
 * `/add`/`/add-temp`/Nova memória, com uma bandeirinha de marcador (12×16)
 * antes do texto, preenchida em `acao` no claro e `usuario` no escuro.
 */
@Composable
fun SavedMessage(text: String, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    val corBandeira = if (colors.dark) colors.usuario else colors.acao
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(modifier = Modifier.padding(top = 4.dp).size(width = 12.dp, height = 16.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(size.width / 2f, size.height * 0.75f)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, corBandeira)
        }
        Text(text, style = solarisText.corpo, color = colors.texto)
    }
}

private fun androidx.compose.ui.text.TextStyle.toSpanStyle() = androidx.compose.ui.text.SpanStyle(
    fontFamily = fontFamily, fontWeight = fontWeight, fontSize = fontSize,
)
