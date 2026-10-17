package dev.solaris.memory.components

import androidx.compose.runtime.Composable
import java.awt.Desktop
import java.io.File

@Composable
actual fun rememberExternalOpener(): (bytes: ByteArray, filename: String, mimeType: String) -> Unit {
    return { bytes, filename, _ ->
        val temp = File.createTempFile("solaris-anexo-", "-$filename")
        temp.writeBytes(bytes)
        temp.deleteOnExit()
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(temp)
        }
    }
}
