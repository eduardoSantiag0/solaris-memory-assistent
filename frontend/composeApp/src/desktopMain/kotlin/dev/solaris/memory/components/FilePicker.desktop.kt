package dev.solaris.memory.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberFilePicker(onPicked: (PickedFile) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    return {
        scope.launch(Dispatchers.IO) {
            // JFileChooser is Swing — must be shown on the EDT, not the
            // Compose render thread, hence invokeAndWait from this
            // background coroutine.
            var picked: PickedFile? = null
            SwingUtilities.invokeAndWait {
                val chooser = JFileChooser().apply {
                    fileFilter = FileNameExtensionFilter("Imagens e PDF", "png", "jpg", "jpeg", "gif", "webp", "pdf")
                    isAcceptAllFileFilterUsed = false
                }
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    val file = chooser.selectedFile
                    picked = PickedFile(file.name, mimeTypeOf(file), file.readBytes())
                }
            }
            picked?.let(onPicked)
        }
    }
}

private fun mimeTypeOf(file: File): String = when (file.extension.lowercase()) {
    "png" -> "image/png"
    "jpg", "jpeg" -> "image/jpeg"
    "gif" -> "image/gif"
    "webp" -> "image/webp"
    "pdf" -> "application/pdf"
    else -> "application/octet-stream"
}
