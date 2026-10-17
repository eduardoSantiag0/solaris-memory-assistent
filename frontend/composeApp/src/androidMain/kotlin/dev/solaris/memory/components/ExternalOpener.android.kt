package dev.solaris.memory.components

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberExternalOpener(): (bytes: ByteArray, filename: String, mimeType: String) -> Unit {
    val context = LocalContext.current
    return { bytes, filename, mimeType ->
        val dir = File(context.cacheDir, "anexos").apply { mkdirs() }
        val file = File(dir, filename)
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
