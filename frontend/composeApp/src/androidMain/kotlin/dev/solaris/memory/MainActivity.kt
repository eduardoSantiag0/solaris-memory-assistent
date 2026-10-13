package dev.solaris.memory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.solaris.memory.settings.AndroidSettingsContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSettingsContext.appContext = applicationContext
        setContent {
            App()
        }
    }
}
