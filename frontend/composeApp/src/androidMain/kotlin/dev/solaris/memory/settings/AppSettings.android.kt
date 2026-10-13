package dev.solaris.memory.settings

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

/** Set once in MainActivity.onCreate, before the first [createPlatformSettings] call. */
object AndroidSettingsContext {
    lateinit var appContext: Context
}

actual fun createPlatformSettings(): Settings {
    val prefs = AndroidSettingsContext.appContext.getSharedPreferences("solaris_memory_settings", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(prefs)
}
