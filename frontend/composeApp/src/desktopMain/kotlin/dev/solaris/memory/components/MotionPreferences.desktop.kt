package dev.solaris.memory.components

import androidx.compose.runtime.Composable

/** Desktop has no equivalent OS-wide "reduce motion" toggle to read. */
@Composable
actual fun animacoesReduzidas(): Boolean = false
