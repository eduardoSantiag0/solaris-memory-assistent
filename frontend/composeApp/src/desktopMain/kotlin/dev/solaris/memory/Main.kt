package dev.solaris.memory

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "solaris-memory-assistent") {
        App()
    }
}
