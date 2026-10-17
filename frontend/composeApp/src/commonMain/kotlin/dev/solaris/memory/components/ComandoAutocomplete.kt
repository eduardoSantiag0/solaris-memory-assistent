package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
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

/**
 * HANDOFF.md tela 03 (Correções 14/10 item 9): exatamente estes três /add* —
 * não existe /add-temp-sensitive no chat, só pela folha Nova memória.
 * /event foi adicionado depois (agrupamento manual de memórias em eventos):
 * cria o evento se o título ainda não existir (match por título, sem
 * diferenciar maiúsculas/minúsculas) e associa a memória nova a ele.
 */
private val COMANDOS = listOf(
    ComandoSugestao(SolarisIcon.PERMANENTE, "/add", "<texto>", "Memória permanente"),
    ComandoSugestao(SolarisIcon.TEMPORARIA, "/add-temp", "<dias> <texto>", "Temporária · sai das respostas depois do prazo"),
    ComandoSugestao(SolarisIcon.SENSIVEL, "/add-sensitive", "<texto>", "Permanente e sensível · mascarada nas respostas"),
    ComandoSugestao(SolarisIcon.DOCUMENTO, "/event", "<evento> | <texto>", "Memória permanente associada a um evento (cria se não existir)"),
)

/** True while the composer field should show the command autocomplete (HANDOFF.md tela 03, RF-15). */
fun deveMostrarAutocomplete(input: String): Boolean = input.startsWith("/") && !input.contains(' ')

// Correção 16/10: apagar até o "-" de /add-temp ou /add-sensitive e seguir
// digitando (ex.: "/addsensitive") não casava com startsWith puro — compara
// ignorando hífen pra aceitar os dois jeitos de digitar.
private fun ComandoSugestao.casaCom(input: String) = comando.replace("-", "").startsWith(input.replace("-", ""))

/** Comandos (só o texto, ex. "/add-temp") que casam com `input` agora, na mesma ordem exibida — usado pelo ChatScreen pra navegar com teclado (setas/Tab/Enter) sem duplicar a lógica de filtro. */
fun comandosFiltrados(input: String): List<String> = COMANDOS.filter { it.casaCom(input) }.map { it.comando }

/**
 * HANDOFF.md tela 03. Painel fixo acima do Composer, não um popup flutuante
 * — mesma lógica de "o conteúdo empurra" já usada pelo resto do chat.
 */
@Composable
fun ComandoAutocomplete(input: String, indiceSelecionado: Int = 0, onSelecionar: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    val opcoes = COMANDOS.filter { it.casaCom(input) }
    if (opcoes.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            // Teto de altura + rolagem interna — nunca deixa o painel ficar maior
            // que a tela disponível (ex.: teclado aberto numa tela baixa) e
            // escondido atrás do teclado ou cortado embaixo (achado 16/10).
            .heightIn(max = 280.dp)
            .verticalScroll(rememberScrollState())
            .background(colors.superficie)
            .drawBehind {
                drawLine(colors.texto, Offset(0f, 1.dp.toPx() / 2), Offset(size.width, 1.dp.toPx() / 2), strokeWidth = 2.dp.toPx())
            }
            .semantics { role = Role.DropdownList },
    ) {
        opcoes.forEachIndexed { index, opcao ->
            val destacada = index == indiceSelecionado.coerceIn(0, opcoes.size - 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (destacada) colors.usuario else colors.superficie)
                    .let { if (index < opcoes.size - 1) it.border(width = 0.dp, color = colors.filete) else it }
                    .clickable { onSelecionar("${opcao.comando} ") }
                    // mergeDescendants: sem isso o TalkBack lia cada Text da linha
                    // (comando, placeholder, descrição) como 3 paradas separadas —
                    // precisa testar com leitor de tela real no device, isto só
                    // garante que o nó de acessibilidade é um só (achado 16/10).
                    .semantics(mergeDescendants = true) {
                        selected = destacada
                        contentDescription = "${opcao.comando} ${opcao.placeholder}. ${opcao.descricao}"
                    }
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
