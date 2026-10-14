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
import androidx.compose.runtime.rememberCoroutineScope
import dev.solaris.memory.components.AppHeader
import dev.solaris.memory.components.BottomNav
import dev.solaris.memory.components.NavTab
import dev.solaris.memory.screens.AppearanceScreen
import dev.solaris.memory.screens.ChatScreen
import dev.solaris.memory.screens.MemoriesScreen
import dev.solaris.memory.screens.MemoryDetailScreen
import dev.solaris.memory.screens.SettingsScreen
import dev.solaris.memory.settings.AppSettings
import dev.solaris.memory.settings.createPlatformSettings
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisTheme
import kotlinx.coroutines.launch

private sealed class SubScreen {
    object None : SubScreen()
    object Aparencia : SubScreen()
    data class Detalhe(val memoriaId: String) : SubScreen()
}

/**
 * HANDOFF.md section 4: shell = AppHeader + conteúdo + BottomNav. Aparência
 * (tela 09) não é uma 4ª aba, é alcançada a partir de Ajustes - continua
 * mostrando AJUSTES como selecionado, com botão voltar no cabeçalho.
 */
@Composable
fun App() {
    val viewModel = remember { AppViewModel(AppSettings(createPlatformSettings())) }
    var tab by rememberSaveable { mutableStateOf(NavTab.CHAT) }
    var sub by remember { mutableStateOf<SubScreen>(SubScreen.None) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.tryRestoreSession()
    }

    SolarisTheme(palette = viewModel.palette, mode = viewModel.themeMode) {
        val colors = LocalSolarisColors.current

        if (viewModel.isRestoringSession) {
            Box(modifier = Modifier.fillMaxSize().background(colors.fundo), contentAlignment = Alignment.Center) {
                Text("Carregando...", color = colors.texto)
            }
            return@SolarisTheme
        }

        if (!viewModel.isLoggedIn) {
            Column(modifier = Modifier.fillMaxSize().background(colors.fundo)) {
                AppHeader(titulo = "solaris", vpnOk = false)
                SettingsScreen(viewModel, onAbrirAparencia = {})
            }
            return@SolarisTheme
        }

        Column(modifier = Modifier.fillMaxSize().background(colors.fundo)) {
            val subAtual = sub
            val memoriaDetalhe = (subAtual as? SubScreen.Detalhe)?.let { d -> viewModel.memories.find { it.id == d.memoriaId } }
            val titulo = when {
                subAtual is SubScreen.Aparencia -> "Aparência"
                subAtual is SubScreen.Detalhe -> "Ficha Nº ${(subAtual.memoriaId).takeLast(4).uppercase()}"
                tab == NavTab.CHAT -> "solaris"
                tab == NavTab.MEMORIAS -> "Memórias"
                else -> "Ajustes"
            }
            val contador = if (tab == NavTab.MEMORIAS && subAtual is SubScreen.None) viewModel.memories.size else null
            AppHeader(
                titulo = titulo,
                contador = contador,
                onVoltar = if (subAtual !is SubScreen.None) ({ sub = SubScreen.None }) else null,
                vpnOk = true,
            )

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                when {
                    subAtual is SubScreen.Aparencia -> AppearanceScreen(viewModel)
                    subAtual is SubScreen.Detalhe && memoriaDetalhe != null -> MemoryDetailScreen(
                        memory = memoriaDetalhe,
                        onExcluido = {
                            sub = SubScreen.None
                            scope.launch { viewModel.deleteMemory(memoriaDetalhe.id) }
                        },
                    )
                    tab == NavTab.CHAT -> ChatScreen(viewModel)
                    tab == NavTab.MEMORIAS -> MemoriesScreen(viewModel, onAbrirDetalhe = { id -> sub = SubScreen.Detalhe(id) })
                    tab == NavTab.AJUSTES -> SettingsScreen(viewModel, onAbrirAparencia = { sub = SubScreen.Aparencia })
                }
            }

            BottomNav(selected = tab, onSelect = { tab = it; sub = SubScreen.None })
        }
    }
}
