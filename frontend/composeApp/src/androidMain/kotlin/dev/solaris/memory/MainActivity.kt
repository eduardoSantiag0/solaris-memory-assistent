package dev.solaris.memory

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import dev.solaris.memory.settings.AndroidSettingsContext

/** FragmentActivity (not plain ComponentActivity) because androidx.biometric.BiometricPrompt needs one. */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSettingsContext.appContext = applicationContext
        setContent {
            App()
        }
    }
}
