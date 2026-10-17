package dev.solaris.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.solaris.memory.components.AppBackHandler
import dev.solaris.memory.components.AppHeader
import dev.solaris.memory.components.BackHeader
import dev.solaris.memory.components.PrimaryButton
import dev.solaris.memory.components.ScreenshotGuard
import dev.solaris.memory.components.SecaoPrincipal
import dev.solaris.memory.components.SectionTabs
import dev.solaris.memory.components.rememberSensitiveGate
import dev.solaris.memory.screens.AppearanceScreen
import dev.solaris.memory.screens.ChatScreen
import dev.solaris.memory.screens.EventDetailScreen
import dev.solaris.memory.screens.EventsScreen
import dev.solaris.memory.screens.MemoriesScreen
import dev.solaris.memory.screens.MemoryDetailScreen
import dev.solaris.memory.screens.SettingsScreen
import dev.solaris.memory.screens.SplashScreen
import dev.solaris.memory.settings.AppSettings
import dev.solaris.memory.settings.createPlatformSettings
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisTheme
import dev.solaris.memory.theme.solarisText

private sealed class SubScreen {
    object None : SubScreen()
    object Ajustes : SubScreen()
    object Aparencia : SubScreen()
    data class Detalhe(val memoriaId: String) : SubScreen()
    data class EventoDetalhe(val eventoId: String) : SubScreen()
}

/**
 * HANDOFF.md v1.1 section 4: shell = AppHeader + SectionTabs (Conversa ·
 * Memórias) + conteúdo. Não há mais navegação inferior — Ajustes é
 * alcançado pela engrenagem do cabeçalho, não é uma aba, por isso vive em
 * `sub`, não em `secao`.
 */
@Composable
fun App() {
    val viewModel = remember { AppViewModel(AppSettings(createPlatformSettings())) }
    var secao by rememberSaveable { mutableStateOf(SecaoPrincipal.CONVERSA) }
    var sub by remember { mutableStateOf<SubScreen>(SubScreen.None) }

    LaunchedEffect(Unit) {
        viewModel.tryRestoreSession()
    }

    SolarisTheme(palette = viewModel.palette, mode = viewModel.themeMode) {
        val colors = LocalSolarisColors.current
        ScreenshotGuard(enabled = viewModel.blockScreenshots)

        if (viewModel.isRestoringSession) {
            SplashScreen()
            return@SolarisTheme
        }

        if (!viewModel.isLoggedIn) {
            Column(modifier = Modifier.fillMaxSize().background(colors.fundo)) {
                Box(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Solaris",
                        style = solarisText.tituloTela.copy(fontWeight = FontWeight.Normal, fontSize = 17.sp, letterSpacing = 3.sp),
                        color = colors.textoSuave,
                    )
                }
                SettingsScreen(viewModel, onAbrirAparencia = {})
            }
            return@SolarisTheme
        }

        // Achado do audit de UI 15/10: "Pedir biometria ao abrir" existia
        // só como chave decorativa. Reaproveita o mesmo gate de
        // biometria/senha já usado pra revelar valor sensível, agora
        // também na entrada do app quando a chave está ligada.
        var desbloqueado by remember { mutableStateOf(false) }
        var tentativaDesbloqueio by remember { mutableStateOf(0) }
        val autenticarAbertura = rememberSensitiveGate(viewModel)
        LaunchedEffect(viewModel.isLoggedIn, tentativaDesbloqueio) {
            if (viewModel.isLoggedIn) {
                desbloqueado = if (viewModel.requireBiometricOnOpen) autenticarAbertura() else true
            }
        }
        if (viewModel.requireBiometricOnOpen && !desbloqueado) {
            TelaBloqueada(onTentarDeNovo = { tentativaDesbloqueio++ })
            return@SolarisTheme
        }

        Column(modifier = Modifier.fillMaxSize().background(colors.fundo)) {
            val subAtual = sub
            val memoriaDetalhe = (subAtual as? SubScreen.Detalhe)?.let { d -> viewModel.memories.find { it.id == d.memoriaId } }

            AppBackHandler(enabled = subAtual != SubScreen.None) {
                sub = if (subAtual is SubScreen.Aparencia) SubScreen.Ajustes else SubScreen.None
            }

            when {
                subAtual is SubScreen.Ajustes -> BackHeader(titulo = "Ajustes", onVoltar = { sub = SubScreen.None })
                subAtual is SubScreen.Aparencia -> BackHeader(titulo = "Aparência", onVoltar = { sub = SubScreen.Ajustes })
                subAtual is SubScreen.Detalhe -> BackHeader(
                    titulo = "Ficha Nº ${subAtual.memoriaId.takeLast(4).uppercase()}",
                    onVoltar = { sub = SubScreen.None },
                    onAjustes = { sub = SubScreen.Ajustes },
                )
                subAtual is SubScreen.EventoDetalhe -> BackHeader(
                    titulo = "Evento",
                    onVoltar = { sub = SubScreen.None },
                    onAjustes = { sub = SubScreen.Ajustes },
                )
                else -> {
                    AppHeader(onAjustes = { sub = SubScreen.Ajustes }, vpnOk = viewModel.isConnected)
                    SectionTabs(
                        selecionada = secao,
                        contadorMemorias = viewModel.memories.size,
                        contadorEventos = viewModel.events.size,
                        onSelecionar = { secao = it },
                    )
                }
            }

            // Achado do audit de UI 15/10: falha ao atualizar a lista ou ao
            // excluir não aparecia em lugar nenhum — `statusMessage` era
            // escrito mas nunca lido por nenhuma tela. Toque pra descartar.
            viewModel.statusMessage?.let { mensagem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.acao)
                        .clickable { viewModel.statusMessage = null }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(mensagem, style = solarisText.corpo, color = colors.textoSobreAcao, modifier = Modifier.weight(1f))
                    Text("✕", style = solarisText.corpo, color = colors.textoSobreAcao, modifier = Modifier.padding(start = 12.dp))
                }
            }

            val conteudoModifier = if (subAtual == SubScreen.None) {
                // Pedido do usuário em 15/10: trocar Conversa/Memórias arrastando o
                // dedo para o lado, não só tocando nas abas.
                Modifier.weight(1f).fillMaxSize().pointerInput(secao) {
                    var arrasteAcumulado = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { arrasteAcumulado = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            arrasteAcumulado += dragAmount
                        },
                        onDragEnd = {
                            val limiar = 72.dp.toPx()
                            if (arrasteAcumulado <= -limiar) secao = SecaoPrincipal.MEMORIAS
                            if (arrasteAcumulado >= limiar) secao = SecaoPrincipal.CONVERSA
                        },
                    )
                }
            } else {
                Modifier.weight(1f).fillMaxSize()
            }

            Box(modifier = conteudoModifier) {
                when {
                    subAtual is SubScreen.Ajustes -> SettingsScreen(viewModel, onAbrirAparencia = { sub = SubScreen.Aparencia })
                    subAtual is SubScreen.Aparencia -> AppearanceScreen(viewModel)
                    subAtual is SubScreen.Detalhe && memoriaDetalhe != null -> MemoryDetailScreen(
                        memory = memoriaDetalhe,
                        viewModel = viewModel,
                        // A exclusão em si roda dentro de MemoryDetailScreen (que já
                        // tem viewModel e memory) e só chama isto depois de confirmar
                        // sucesso — antes a tela voltava pra lista e disparava a
                        // exclusão em paralelo, sem esperar o resultado (achado 15/10).
                        onExcluido = { sub = SubScreen.None },
                        onAbrirEvento = { id -> sub = SubScreen.EventoDetalhe(id) },
                    )
                    subAtual is SubScreen.EventoDetalhe -> EventDetailScreen(
                        eventId = subAtual.eventoId,
                        viewModel = viewModel,
                        onAbrirMemoria = { id -> sub = SubScreen.Detalhe(id) },
                        onExcluido = { sub = SubScreen.None },
                    )
                    secao == SecaoPrincipal.CONVERSA -> ChatScreen(viewModel, onAbrirDetalhe = { id -> sub = SubScreen.Detalhe(id) })
                    secao == SecaoPrincipal.MEMORIAS -> MemoriesScreen(viewModel, onAbrirDetalhe = { id -> sub = SubScreen.Detalhe(id) })
                    secao == SecaoPrincipal.EVENTOS -> EventsScreen(viewModel, onAbrirDetalhe = { id -> sub = SubScreen.EventoDetalhe(id) })
                }
            }
        }
    }
}

@Composable
private fun TelaBloqueada(onTentarDeNovo: () -> Unit) {
    val colors = LocalSolarisColors.current
    Column(
        modifier = Modifier.fillMaxSize().background(colors.fundo).padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Autenticação necessária", style = solarisText.tituloDialogo, color = colors.texto)
        Text(
            "Confirme sua identidade para abrir o Solaris.",
            style = solarisText.corpo,
            color = colors.textoSecundario,
            modifier = Modifier.padding(top = 8.dp),
        )
        PrimaryButton(text = "Tentar de novo", onClick = onTentarDeNovo, modifier = Modifier.padding(top = 20.dp))
    }
}
