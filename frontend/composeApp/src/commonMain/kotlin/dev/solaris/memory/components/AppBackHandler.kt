package dev.solaris.memory.components

import androidx.compose.runtime.Composable

/**
 * Intercepta o botão/gesto de voltar do sistema (Android) para fechar a
 * subtela atual em vez de sair do app — pedido do usuário em 15/10: "o
 * voltar não está voltando pra página anterior". No Desktop não existe
 * gesto de voltar do sistema, então a actual lá é um no-op.
 */
@Composable
expect fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit)
