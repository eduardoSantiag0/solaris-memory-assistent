package dev.solaris.memory.components

import androidx.compose.runtime.Composable

/**
 * Opens an attachment's bytes in whatever external viewer the OS has for
 * its mime type (PDF reader, image viewer) — there is no in-app PDF
 * renderer here. Writes to a temp/cache file first since both platforms'
 * "open externally" APIs need a real file, not raw bytes.
 */
@Composable
expect fun rememberExternalOpener(): (bytes: ByteArray, filename: String, mimeType: String) -> Unit
