package dev.solaris.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.solaris.memory.components.PixelButton
import dev.solaris.memory.screens.ChatScreen
import dev.solaris.memory.screens.MemoriesScreen
import dev.solaris.memory.screens.SettingsScreen
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisTheme

private enum class Tab { CHAT, MEMORIES, SETTINGS }

/**
 * Three sections from doc 11.3 (Navegação, Conversação, Base de memórias)
 * adapted to a small screen as a single-pane-plus-bottom-tabs layout
 * (11.3: "não exigir a exibição simultânea dos três painéis"). Works the
 * same on desktop, just with more room around it — mobile is the target,
 * desktop only needs to not break.
 */
@Composable
fun App() {
    val viewModel = remember { AppViewModel() }
    var tab by rememberSaveable { mutableStateOf(Tab.SETTINGS) }

    SolarisTheme(darkMode = viewModel.darkMode) {
        val colors = LocalSolarisColors.current

        if (!viewModel.isLoggedIn) {
            SettingsScreen(viewModel)
            return@SolarisTheme
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                when (tab) {
                    Tab.CHAT -> ChatScreen(viewModel)
                    Tab.MEMORIES -> MemoriesScreen(viewModel)
                    Tab.SETTINGS -> SettingsScreen(viewModel)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceVariant)
                    .padding(8.dp),
            ) {
                TabButton("Chat", tab == Tab.CHAT, Modifier.weight(1f)) { tab = Tab.CHAT }
                TabButton("Memórias", tab == Tab.MEMORIES, Modifier.weight(1f).padding(horizontal = 6.dp)) { tab = Tab.MEMORIES }
                TabButton("Ajustes", tab == Tab.SETTINGS, Modifier.weight(1f)) { tab = Tab.SETTINGS }
            }
        }
    }
}

@Composable
private fun TabButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = LocalSolarisColors.current
    PixelButton(
        text = label,
        onClick = onClick,
        backgroundColor = if (selected) colors.userBubble else colors.surface,
        modifier = modifier,
    )
}
