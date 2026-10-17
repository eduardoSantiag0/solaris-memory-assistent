package dev.solaris.memory.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

actual fun suportaCameraDireta(): Boolean = true

@Composable
actual fun rememberCameraCapture(onCaptured: (PickedFile) -> Unit): () -> Unit {
    val context = LocalContext.current
    var arquivoPendente by remember { mutableStateOf<File?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { sucesso ->
        val arquivo = arquivoPendente
        if (sucesso && arquivo != null) {
            onCaptured(PickedFile(arquivo.name, "image/jpeg", arquivo.readBytes()))
        }
        arquivo?.delete()
        arquivoPendente = null
    }

    return {
        val dir = File(context.cacheDir, "anexos").apply { mkdirs() }
        val arquivo = File(dir, "foto_${System.currentTimeMillis()}.jpg")
        arquivoPendente = arquivo
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivo)
        launcher.launch(uri)
    }
}
