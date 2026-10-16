package dev.solaris.memory.components

import androidx.compose.runtime.Composable

/**
 * Ajustes > Privacidade > "Bloquear capturas de tela" — achado do audit de
 * UI 15/10: a chave já existia na tela mas não tinha nenhum efeito real.
 * Android usa `FLAG_SECURE` (bloqueia screenshot, gravação de tela e
 * aparece em branco no seletor de apps recentes); Desktop não tem uma API
 * multiplataforma simples equivalente, então a actual lá é um no-op — o
 * switch continua persistido, só sem efeito nesse alvo.
 */
@Composable
expect fun ScreenshotGuard(enabled: Boolean)
