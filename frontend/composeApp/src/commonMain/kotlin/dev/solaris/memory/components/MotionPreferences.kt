package dev.solaris.memory.components

import androidx.compose.runtime.Composable

/** Whether the OS has animations turned off/reduced — `SolarisHeaderMark` freezes on frame 0 when true. */
@Composable
expect fun animacoesReduzidas(): Boolean
