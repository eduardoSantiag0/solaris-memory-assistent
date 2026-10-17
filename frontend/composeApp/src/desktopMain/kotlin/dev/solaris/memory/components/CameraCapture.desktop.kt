package dev.solaris.memory.components

import androidx.compose.runtime.Composable

/** Desktop has no single standard camera-capture API to delegate to — the file picker already covers attaching a photo taken elsewhere. */
actual fun suportaCameraDireta(): Boolean = false

@Composable
actual fun rememberCameraCapture(onCaptured: (PickedFile) -> Unit): () -> Unit = {}
