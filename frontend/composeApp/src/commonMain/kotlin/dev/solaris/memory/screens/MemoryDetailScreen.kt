package dev.solaris.memory.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.components.ConfirmDialog
import dev.solaris.memory.components.DangerButton
import dev.solaris.memory.components.MaskedValue
import dev.solaris.memory.components.MonoOutlineChip
import dev.solaris.memory.components.NovaMemoriaSheet
import dev.solaris.memory.components.SecondaryButton
import dev.solaris.memory.components.Stamp
import dev.solaris.memory.components.StampType
import dev.solaris.memory.components.decodeImageBitmap
import dev.solaris.memory.components.rememberExternalOpener
import dev.solaris.memory.network.AttachmentResponse
import dev.solaris.memory.network.EventResponse
import dev.solaris.memory.network.MemoryResponse
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.SolarisIconGlyph
import dev.solaris.memory.theme.solarisText
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** HANDOFF.md tela 06. "Editar" reaproveita a folha Nova memória em modo edição (RF-09). */
@Composable
fun MemoryDetailScreen(memory: MemoryResponse, viewModel: AppViewModel, onExcluido: () -> Unit, onAbrirEvento: (String) -> Unit = {}) {
    var confirmandoExclusao by remember { mutableStateOf(false) }
    var excluindo by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    MemoryDetailContent(memory, viewModel, onPedirExclusao = { confirmandoExclusao = true }, onEditar = { editando = true }, onAbrirEvento = onAbrirEvento)

    if (confirmandoExclusao) {
        val sensivel = memory.sensitivity == "SENSITIVE"
        val texto = if (sensivel) {
            "Este item sensível sai das buscas na hora. Não dá para desfazer."
        } else {
            val inicio = memory.content.take(40).let { if (memory.content.length > 40) "$it..." else it }
            "“$inicio” sai das buscas na hora. Não dá para desfazer."
        }
        ConfirmDialog(
            titulo = "Excluir esta ficha?",
            texto = texto,
            // Correção 15/10: antes a tela já voltava pra lista e disparava
            // a exclusão em paralelo sem esperar — uma falha de rede/servidor
            // passava batida, parecendo sucesso. Agora só navega de volta
            // (onExcluido) depois de confirmar que excluiu de verdade;
            // falha fica visível pelo banner de statusMessage em App.kt.
            confirmarLabel = if (excluindo) "Excluindo..." else "Excluir",
            onConfirmar = {
                if (!excluindo) {
                    excluindo = true
                    scope.launch {
                        val sucesso = viewModel.deleteMemory(memory.id)
                        excluindo = false
                        confirmandoExclusao = false
                        if (sucesso) onExcluido()
                    }
                }
            },
            onCancelar = { confirmandoExclusao = false },
        )
    }

    if (editando) {
        NovaMemoriaSheet(
            viewModel = viewModel,
            editando = memory,
            onDismiss = { editando = false },
            onSalvar = { resultado -> viewModel.updateMemoryFromForm(memory.id, resultado) },
        )
    }
}

@Composable
private fun MemoryDetailContent(memory: MemoryResponse, viewModel: AppViewModel, onPedirExclusao: () -> Unit, onEditar: () -> Unit, onAbrirEvento: (String) -> Unit) {
    val colors = LocalSolarisColors.current
    val isTemporaria = memory.expiresAt != null
    val isVencida = memory.status == "EXPIRED" || memory.status == "DELETED"
    val isSensivel = memory.sensitivity == "SENSITIVE"

    val tracejadoColor = colors.tracejado
    val divisoriaColor = colors.divisoria

    var anexo by remember { mutableStateOf<AttachmentResponse?>(null) }
    var anexoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var eventosDaMemoria by remember { mutableStateOf<List<EventResponse>>(emptyList()) }
    val abrirExternamente = rememberExternalOpener()

    LaunchedEffect(memory.id) {
        anexo = null
        anexoBytes = null
        if (memory.hasAttachment) {
            try {
                anexo = viewModel.api.getAttachment(memory.id)
                anexoBytes = viewModel.api.getAttachmentBytes(memory.id)
            } catch (e: Exception) {
                // Sem bloquear a tela por causa do anexo — o conteúdo principal da ficha segue visível.
            }
        }
        eventosDaMemoria = try {
            viewModel.api.eventsForMemory(memory.id)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Pedido do usuário em 15/10: "ainda não consigo editar ou excluir
    // memórias permanentes" — causa real era este Column não rolar: texto
    // registrado grande (destaqueFicha, 23sp) + metadados empurravam os
    // botões Editar/Excluir além do fim da tela, sem jeito de alcançá-los.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.fundo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Box {
            Column(modifier = Modifier.fillMaxWidth().background(colors.superficie)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(tracejadoColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text("TEXTO REGISTRADO", style = solarisText.meta, color = colors.texto)
                    Row(modifier = Modifier.padding(top = 6.dp)) {
                        Stamp(
                            when {
                                isVencida -> StampType.VENCIDA
                                isTemporaria -> StampType.TEMPORARIA
                                else -> StampType.PERMANENTE
                            },
                        )
                        if (isSensivel) {
                            Spacer(Modifier.width(6.dp))
                            Stamp(StampType.SENSIVEL)
                        }
                    }
                }
                if (isSensivel) {
                    MaskedValue(memory.content, viewModel, modifier = Modifier.padding(top = 6.dp, bottom = 8.dp))
                } else {
                    Text(
                        memory.content,
                        style = solarisText.destaqueFicha,
                        color = colors.texto,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 18.dp, bottom = 30.dp),
                    )
                }
            }
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

        val expiresAt = memory.expiresAt
        if (isTemporaria && !isVencida && expiresAt != null) {
            Spacer(Modifier.height(24.dp))
            Text("VENCE EM", style = solarisText.meta, color = colors.texto)
            val (contagem, proporcao) = contagemRegressiva(memory.createdAt, expiresAt)
            Text(contagem, style = solarisText.contador, color = colors.texto, modifier = Modifier.padding(top = 4.dp))
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(colors.trilho).padding(top = 8.dp)) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(proporcao.coerceIn(0f, 1f)).background(colors.texto))
            }
            Text(
                "Até ${formatarDataHora(expiresAt)}. Depois disso, não entra mais nas respostas.",
                style = solarisText.corpo.copy(fontSize = 15.sp),
                color = colors.texto,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(divisoriaColor, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                }
                .padding(top = 18.dp),
        ) {
            MetaRow("REGISTRADA", formatarDataHora(memory.createdAt))
            MetaRow("EVENTO", memory.eventAt?.let { formatarDataHora(it) } ?: "Não informado")
            MetaRow("ATUALIZADA", if (memory.updatedAt != memory.createdAt) formatarDataHora(memory.updatedAt) else "—")
            MetaRow("STATUS", if (isVencida) "Vencida" else "Ativa")
        }

        val anexoAtual = anexo
        if (anexoAtual != null) {
            Spacer(Modifier.height(24.dp))
            Text("ANEXO", style = solarisText.meta, color = colors.texto, modifier = Modifier.padding(bottom = 8.dp))
            val bitmap = remember(anexoBytes) { anexoBytes?.let { decodeImageBitmap(it) } }
            Column(modifier = Modifier.fillMaxWidth().background(colors.superficie).padding(12.dp)) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = anexoAtual.filename,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp)
                            .clickable { anexoBytes?.let { abrirExternamente(it, anexoAtual.filename, anexoAtual.contentType) } },
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SolarisIconGlyph(SolarisIcon.DOCUMENTO, colors.texto)
                        Text(anexoAtual.filename, style = solarisText.corpo, color = colors.texto, modifier = Modifier.padding(start = 10.dp))
                    }
                }
                Text(anexoAtual.description, style = solarisText.corpo, color = colors.texto, modifier = Modifier.padding(top = 10.dp))
                MonoOutlineChip(
                    text = "Abrir",
                    onClick = { anexoBytes?.let { abrirExternamente(it, anexoAtual.filename, anexoAtual.contentType) } },
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }

        if (eventosDaMemoria.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("PERTENCE A", style = solarisText.meta, color = colors.texto, modifier = Modifier.padding(bottom = 8.dp))
            eventosDaMemoria.forEach { evento ->
                Text(
                    evento.title,
                    style = solarisText.corpo,
                    color = colors.acao,
                    modifier = Modifier.padding(bottom = 6.dp).clickable { onAbrirEvento(evento.id) },
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 20.dp)) {
            SecondaryButton(text = "Editar", onClick = onEditar, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            DangerButton(text = "Excluir", onClick = onPedirExclusao, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetaRow(label: String, valor: String) {
    val colors = LocalSolarisColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = solarisText.meta, color = colors.texto)
        Text(valor, style = solarisText.corpo.copy(fontSize = 15.sp), color = colors.texto)
    }
}

private fun formatarDataHora(iso: String): String {
    return try {
        val dt = Instant.parse(iso).toLocalDateTime(TimeZone.currentSystemDefault())
        "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}/${dt.year} · " +
            "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        iso
    }
}

private fun contagemRegressiva(createdAtIso: String, expiresAtIso: String): Pair<String, Float> {
    return try {
        val criado = Instant.parse(createdAtIso)
        val vence = Instant.parse(expiresAtIso)
        val agora = Clock.System.now()
        val restante = vence - agora
        val total = vence - criado

        if (restante.isNegative()) return "0m" to 0f

        val dias = restante.inWholeDays
        val horas = restante.inWholeHours % 24
        val minutos = restante.inWholeMinutes % 60
        val texto = buildString {
            if (dias > 0) append("${dias}d ")
            append("${horas}h ")
            append("${minutos}m")
        }.trim()

        val proporcao = if (total.inWholeSeconds > 0) (restante.inWholeSeconds.toFloat() / total.inWholeSeconds.toFloat()) else 0f
        texto to proporcao
    } catch (e: Exception) {
        "—" to 0f
    }
}
