package dev.solaris.memory.components

import androidx.compose.runtime.Composable

/** Desktop não tem gesto/botão de voltar do sistema para interceptar. */
@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
}
