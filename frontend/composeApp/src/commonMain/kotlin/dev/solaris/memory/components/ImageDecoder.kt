package dev.solaris.memory.components

import androidx.compose.ui.graphics.ImageBitmap

/** Decodes raw image bytes (jpeg/png/webp/gif) for inline preview. Returns null on anything that isn't a decodable image (e.g. a PDF). */
expect fun decodeImageBitmap(bytes: ByteArray): ImageBitmap?
