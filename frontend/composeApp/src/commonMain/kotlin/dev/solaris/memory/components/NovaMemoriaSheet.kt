package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.solarisText
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours

private enum class TipoMemoria { PERMANENTE, TEMPORARIA }

/**
 * HANDOFF.md tela 04. Overlay próprio em vez de Material3 ModalBottomSheet,
 * já que cantos retos e o véu exato dos tokens são mais simples de garantir
 * desenhando a folha na mão do que lutando contra a forma padrão do Material.
 *
 * Seletor de data/hora do evento ("Definir") ainda não implementado — fica
 * para quando houver um DatePicker/TimePicker próprio do tema (HANDOFF.md
 * seção 9, "fora deste handoff").
 */
@Composable
fun NovaMemoriaSheet(onDismiss: () -> Unit, onSalvar: suspend (content: String, ttlDays: Int?, sensitive: Boolean) -> String?) {
    val colors = LocalSolarisColors.current
    val scope = rememberCoroutineScope()

    var tipo by remember { mutableStateOf(TipoMemoria.TEMPORARIA) }
    var dias by remember { mutableStateOf(2) }
    var conteudo by remember { mutableStateOf("") }
    var sensivel by remember { mutableStateOf(false) }
    var salvando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.veu)
            .clickable(onClick = onDismiss),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .background(colors.fundo)
                .clickable(enabled = false) {} // absorve toques para não fechar ao tocar dentro
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp, bottom = 12.dp)
                    .size(width = 40.dp, height = 3.dp)
                    .background(colors.filete),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Nova memória", style = solarisText.tituloFolha, color = colors.texto)
                IconSquareButton(SolarisIcon.FECHAR, "Fechar", onDismiss)
            }

            Spacer(Modifier.height(16.dp))
            TipoCard("Permanente", "Fica até você excluir.", tipo == TipoMemoria.PERMANENTE) { tipo = TipoMemoria.PERMANENTE }
            Spacer(Modifier.height(8.dp))
            TipoCard("Temporária", "Sai das respostas depois do prazo.", tipo == TipoMemoria.TEMPORARIA) { tipo = TipoMemoria.TEMPORARIA }

            if (tipo == TipoMemoria.TEMPORARIA) {
                HorizontalRule(topPadding = 16.dp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("VALE POR", style = solarisText.meta, color = colors.textoSecundario)
                        Text(ateQuando(dias), style = solarisText.corpo, color = colors.texto)
                    }
                    DiasStepper(dias = dias, onChange = { dias = it })
                }
                HorizontalRule()
            }

            Text("O QUE VOCÊ QUER LEMBRAR?", style = solarisText.meta, color = colors.textoSecundario, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(colors.superficie)
                    .border(1.dp, colors.filete)
                    .padding(12.dp),
            ) {
                BasicTextField(
                    value = conteudo,
                    onValueChange = { conteudo = it; erro = null },
                    textStyle = solarisText.corpo.copy(color = colors.texto),
                    cursorBrush = SolidColor(colors.texto),
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("DATA DO EVENTO", style = solarisText.meta, color = colors.textoSecundario)
                    Text("Não informada", style = solarisText.corpo, color = colors.texto)
                }
                OutlineChip(text = "Definir", onClick = { /* TODO: date/time picker */ })
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp).clickable { sensivel = !sensivel },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(if (sensivel) colors.texto else colors.fundo)
                        .border(1.dp, colors.filete),
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("Marcar como sensível", style = solarisText.corpo, color = colors.texto)
                    Text("MASCARADA NAS RESPOSTAS", style = solarisText.meta, color = colors.textoSecundario)
                }
            }

            if (erro != null) {
                Text(erro!!, style = solarisText.corpo, color = colors.acao, modifier = Modifier.padding(top = 12.dp))
            }

            PrimaryButton(
                text = if (salvando) "Salvando..." else "Salvar memória",
                enabled = conteudo.isNotBlank() && !salvando,
                onClick = {
                    scope.launch {
                        salvando = true
                        val resultado = onSalvar(conteudo, if (tipo == TipoMemoria.TEMPORARIA) dias else null, sensivel)
                        salvando = false
                        if (resultado == null) {
                            onDismiss()
                        } else {
                            erro = resultado
                        }
                    }
                },
                modifier = Modifier.padding(top = 20.dp),
            )

            Text(
                buildAnnotatedString {
                    append("Equivale a ")
                    withStyle(solarisText.comando.toSpanStyle()) {
                        append(if (tipo == TipoMemoria.TEMPORARIA) "/add-temp $dias" else "/add")
                    }
                    append(" no chat")
                },
                style = solarisText.meta,
                color = colors.textoSecundario,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp, bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun TipoCard(titulo: String, descricao: String, selecionado: Boolean, onClick: () -> Unit) {
    val colors = LocalSolarisColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selecionado) colors.usuario else colors.fundo)
            .border(1.dp, colors.filete)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
                .border(1.dp, colors.filete, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selecionado) {
                Box(Modifier.size(10.dp).background(colors.texto, androidx.compose.foundation.shape.CircleShape))
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(titulo, style = solarisText.corpo.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = colors.texto)
            Text(descricao, style = solarisText.corpo, color = colors.texto, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun DiasStepper(dias: Int, onChange: (Int) -> Unit) {
    val colors = LocalSolarisColors.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(44.dp).border(1.dp, colors.filete).clickable(enabled = dias > 1) { onChange((dias - 1).coerceAtLeast(1)) },
            contentAlignment = Alignment.Center,
        ) { Text("−", style = solarisText.tituloDialogo, color = colors.texto) }

        Text("$dias dia${if (dias == 1) "" else "s"}", style = solarisText.corpo.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = colors.texto, modifier = Modifier.padding(horizontal = 16.dp))

        Box(
            modifier = Modifier.size(44.dp).border(1.dp, colors.filete).clickable(enabled = dias < 365) { onChange((dias + 1).coerceAtMost(365)) },
            contentAlignment = Alignment.Center,
        ) { Text("+", style = solarisText.tituloDialogo, color = colors.texto) }
    }
}

@Composable
private fun HorizontalRule(topPadding: androidx.compose.ui.unit.Dp = 0.dp) {
    val colors = LocalSolarisColors.current
    Box(modifier = Modifier.fillMaxWidth().padding(top = topPadding).height(1.dp).background(colors.filete))
}

private fun ateQuando(dias: Int): String {
    val agora = Clock.System.now()
    val alvo = agora.plus((24 * dias).hours).toLocalDateTime(TimeZone.currentSystemDefault())
    val dia = alvo.dayOfMonth.toString().padStart(2, '0')
    val mes = alvo.monthNumber.toString().padStart(2, '0')
    val hora = alvo.hour.toString().padStart(2, '0')
    val min = alvo.minute.toString().padStart(2, '0')
    return "até $dia/$mes, $hora:$min"
}

private fun androidx.compose.ui.text.TextStyle.toSpanStyle() = androidx.compose.ui.text.SpanStyle(
    fontFamily = fontFamily, fontWeight = fontWeight, fontSize = fontSize,
)
