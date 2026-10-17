package dev.solaris.memory.components

import androidx.compose.runtime.Composable

/** Whether this platform can offer a direct camera capture for attachments, besides the file picker (RF: anexos; achado da pesquisa de UI 16/10). */
expect fun suportaCameraDireta(): Boolean

/**
 * Opens the platform camera straight into a photo, skipping the "save to
 * gallery, then pick the file" round trip. Only meaningful where
 * [suportaCameraDireta] is true — call sites must guard with it, since the
 * desktop actual is a no-op launcher.
 */
@Composable
expect fun rememberCameraCapture(onCaptured: (PickedFile) -> Unit): () -> Unit
