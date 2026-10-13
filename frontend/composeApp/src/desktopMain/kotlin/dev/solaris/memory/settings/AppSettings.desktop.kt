package dev.solaris.memory.settings

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

actual fun createPlatformSettings(): Settings {
    return PreferencesSettings(Preferences.userRoot().node("dev/solaris/memory/assistant"))
}
