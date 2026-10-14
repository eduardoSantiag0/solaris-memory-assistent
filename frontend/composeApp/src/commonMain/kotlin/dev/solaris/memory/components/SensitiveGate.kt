package dev.solaris.memory.components

import androidx.compose.runtime.Composable
import dev.solaris.memory.AppViewModel

/**
 * HANDOFF.md 5.4 item 2: confirma a identidade antes de revelar um valor
 * sensível. Android usa `BiometricPrompt` de verdade (com alternativa de
 * credencial do aparelho). Desktop não tem biometria, então usa a senha da
 * conta — único segredo que já existe, já que cada instância deste app é de
 * um usuário só (deploy/README.md PRE-05).
 *
 * Retorna uma função suspensa que mostra o gate e resolve para `true`
 * (liberado) ou `false` (cancelado/falhou), chamada sob demanda a partir de
 * MaskedValue — nunca libera automaticamente.
 */
@Composable
expect fun rememberSensitiveGate(viewModel: AppViewModel): suspend () -> Boolean
