package dev.solaris.memory.components

import androidx.compose.runtime.Composable

/** One file picked by the user for a memory's attachment (RF: anexos). */
data class PickedFile(val name: String, val mimeType: String, val bytes: ByteArray)

/**
 * Opens the platform's native file/document picker restricted to images and
 * PDFs. Returns a launcher: call it to open the picker; [onPicked] fires
 * once the user confirms a choice (never on cancel).
 */
@Composable
expect fun rememberFilePicker(onPicked: (PickedFile) -> Unit): () -> Unit
