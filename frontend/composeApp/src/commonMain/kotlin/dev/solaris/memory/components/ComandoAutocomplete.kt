package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.SolarisIconGlyph
import dev.solaris.memory.theme.solarisText

private data class ComandoSugestao(val icon: SolarisIcon, val comando: String, val placeholder: String, val descricao: String)

private val COMANDOS = listOf(
    ComandoSugestao(SolarisIcon.PERMANENTE, "/add", "<texto>", "Memória permanente"),
    ComandoSugestao(SolarisIcon.TEMPORARIA, "/add-temp", "<dias> <texto>", "Temporária · sai das respostas depois do prazo"),
    ComandoSugestao(SolarisIcon.SENSIVEL, "/add-sensitive", "<texto>", "Permanente · mascarada nas respostas"),
    ComandoSugestao(SolarisIcon.SENSIVEL, "/add-temp-sensitive", "<dias> <texto>", "Temporária e mascarada"),
)

/** True while the composer field should show the command autocomplete (HANDOFF.md tela 03, RF-15). */
fun deveMostrarAutocomplete(input: String): Boolean = input.startsWith("/") && !input.contains(' ')

/**
 * HANDOFF.md tela 03. Painel fixo acima do Composer, não um popup flutuante
 * — mesma lógica de "o conteúdo empurra" já usada pelo resto do chat.
 */
@Composable
fun ComandoAutocomplete(input: String, onSelecionar: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    val opcoes = COMANDOS.filter { it.comando.startsWith(input) }
    if (opcoes.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.superficie)
            .drawBehind {
                drawLine(colors.texto, Offset(0f, 1.dp.toPx() / 2), Offset(size.width, 1.dp.toPx() / 2), strokeWidth = 2.dp.toPx())
            }
            .semantics { role = Role.DropdownList },
    ) {
        opcoes.forEachIndexed { index, opcao ->
            val destacada = index == 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (destacada) colors.usuario else colors.superficie)
                    .let { if (index < opcoes.size - 1) it.border(width = 0.dp, color = colors.filete) else it }
                    .clickable { onSelecionar("${opcao.comando} ") }
                    .semantics { selected = destacada }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SolarisIconGlyph(opcao.icon, colors.texto, size = 18.dp)
                Column {
                    Text(
                        buildAnnotatedString {
                            withStyle(solarisText.comando.toSpanStyle()) { append(opcao.comando) }
                            append(" ${opcao.placeholder}")
                        },
                        style = solarisText.comando.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal),
                        color = colors.texto,
                    )
                    Text(opcao.descricao, style = solarisText.corpo.copy(fontSize = 14.sp), color = colors.texto, modifier = Modifier.padding(top = 2.dp))
                }
            }
            if (index < opcoes.size - 1) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.filete))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SolarisIconGlyph(SolarisIcon.NOVA, colors.textoSuave, size = 14.dp)
            Text("Prefere um formulário? Toque em +", style = solarisText.meta, color = colors.textoSuave)
        }
    }
}

private fun androidx.compose.ui.text.TextStyle.toSpanStyle() = androidx.compose.ui.text.SpanStyle(
    fontFamily = fontFamily, fontWeight = fontWeight, fontSize = fontSize,
)
