package dev.solaris.memory

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import dev.solaris.memory.components.AppHeader
import dev.solaris.memory.components.BackHeader
import dev.solaris.memory.components.SecaoPrincipal
import dev.solaris.memory.components.SectionTabs
import dev.solaris.memory.screens.AppearanceScreen
import dev.solaris.memory.screens.ChatScreen
import dev.solaris.memory.screens.MemoriesScreen
import dev.solaris.memory.screens.MemoryDetailScreen
import dev.solaris.memory.screens.SettingsScreen
import dev.solaris.memory.screens.SplashScreen
import dev.solaris.memory.settings.AppSettings
import dev.solaris.memory.settings.createPlatformSettings
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisTheme
import dev.solaris.memory.theme.solarisText
import kotlinx.coroutines.launch

/**
 * Correções 14/10 item 3: cabeçalho, abas, conversa e compositor vivem numa
 * coluna de no máximo 560dp, centralizada — o alvo principal continua
 * sendo Android em retrato (390dp, bem abaixo do limite, então nada muda
 * lá); fora da coluna, em telas largas (desktop, tablet), só o fundo.
 */
@Composable
private fun CenteredShell(fundo: Color, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(fundo)) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            content = content,
        )
    }
}

private sealed class SubScreen {
    object None : SubScreen()
    object Ajustes : SubScreen()
    object Aparencia : SubScreen()
    data class Detalhe(val memoriaId: String) : SubScreen()
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
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.tryRestoreSession()
    }

    SolarisTheme(palette = viewModel.palette, mode = viewModel.themeMode) {
        val colors = LocalSolarisColors.current

        if (viewModel.isRestoringSession) {
            SplashScreen()
            return@SolarisTheme
        }

        if (!viewModel.isLoggedIn) {
            CenteredShell(colors.fundo) {
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

        CenteredShell(colors.fundo) {
            val subAtual = sub
            val memoriaDetalhe = (subAtual as? SubScreen.Detalhe)?.let { d -> viewModel.memories.find { it.id == d.memoriaId } }

            when {
                subAtual is SubScreen.Ajustes -> BackHeader(titulo = "Ajustes", onVoltar = { sub = SubScreen.None })
                subAtual is SubScreen.Aparencia -> BackHeader(titulo = "Aparência", onVoltar = { sub = SubScreen.Ajustes })
                subAtual is SubScreen.Detalhe -> BackHeader(
                    titulo = "Ficha Nº ${subAtual.memoriaId.takeLast(4).uppercase()}",
                    onVoltar = { sub = SubScreen.None },
                    onAjustes = { sub = SubScreen.Ajustes },
                )
                else -> {
                    AppHeader(onAjustes = { sub = SubScreen.Ajustes })
                    SectionTabs(
                        selecionada = secao,
                        contadorMemorias = viewModel.memories.size,
                        onSelecionar = { secao = it },
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                when {
                    subAtual is SubScreen.Ajustes -> SettingsScreen(viewModel, onAbrirAparencia = { sub = SubScreen.Aparencia })
                    subAtual is SubScreen.Aparencia -> AppearanceScreen(viewModel)
                    subAtual is SubScreen.Detalhe && memoriaDetalhe != null -> MemoryDetailScreen(
                        memory = memoriaDetalhe,
                        viewModel = viewModel,
                        onExcluido = {
                            sub = SubScreen.None
                            scope.launch { viewModel.deleteMemory(memoriaDetalhe.id) }
                        },
                    )
                    secao == SecaoPrincipal.CONVERSA -> ChatScreen(viewModel, onAbrirDetalhe = { id -> sub = SubScreen.Detalhe(id) })
                    secao == SecaoPrincipal.MEMORIAS -> MemoriesScreen(viewModel, onAbrirDetalhe = { id -> sub = SubScreen.Detalhe(id) })
                }
            }
        }
    }
}
