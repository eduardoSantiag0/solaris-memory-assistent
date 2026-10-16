package dev.solaris.memory.settings

import com.russhwolf.settings.Settings

/**
 * Local persistence for session and preferences (dark mode, server URL,
 * auth token) so they survive closing the app, per the "Próximos passos"
 * gap noted after the first UI slice. Backed by SharedPreferences on
 * Android and java.util.prefs on Desktop via multiplatform-settings — same
 * keys, same code, no expect/actual needed beyond constructing the
 * platform-specific [Settings] instance itself.
 * <p>
 * Plaintext on disk, same tradeoff most apps make for a device-local
 * session token: this protects against nothing an unlocked, stolen device
 * doesn't already expose. Encrypting it (EncryptedSharedPreferences on
 * Android) is a later hardening step if ever needed, not a v1 requirement —
 * unlike the backend's field encryption, which defends against other
 * accounts on a *shared* server, a different and higher-priority threat.
 */
class AppSettings(private val settings: Settings) {

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_PALETTE = "palette_id"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USERNAME = "auth_username"
        private const val KEY_REQUIRE_BIOMETRIC_ON_OPEN = "require_biometric_on_open"
        private const val KEY_BLOCK_SCREENSHOTS = "block_screenshots"

        private const val DEFAULT_SERVER_URL = "http://localhost:8080"
    }

    /** "claro" | "escuro" | "sistema" — design-tokens.json temaPadrao. */
    var themeMode: String
        get() = settings.getString(KEY_THEME_MODE, "sistema")
        set(value) = settings.putString(KEY_THEME_MODE, value)

    /** One of Palette.id — design-tokens.json paletaPadrao. */
    var paletteId: String
        get() = settings.getString(KEY_PALETTE, "estacao")
        set(value) = settings.putString(KEY_PALETTE, value)

    var serverUrl: String
        get() = settings.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL)
        set(value) = settings.putString(KEY_SERVER_URL, value)

    var token: String?
        get() = settings.getStringOrNull(KEY_TOKEN)
        set(value) {
            if (value == null) settings.remove(KEY_TOKEN) else settings.putString(KEY_TOKEN, value)
        }

    /** Remembered so the Desktop sensitive-reveal gate (components/SensitiveGate.desktop.kt) can re-verify just the password, not ask for the username again — this app is single-user per instance (deploy/README.md PRE-05). */
    var username: String?
        get() = settings.getStringOrNull(KEY_USERNAME)
        set(value) {
            if (value == null) settings.remove(KEY_USERNAME) else settings.putString(KEY_USERNAME, value)
        }

    /** Ajustes > Privacidade — gate de biometria/senha reaproveitando SensitiveGate, agora exigido também ao abrir o app, não só ao revelar um valor. */
    var requireBiometricOnOpen: Boolean
        get() = settings.getBoolean(KEY_REQUIRE_BIOMETRIC_ON_OPEN, true)
        set(value) = settings.putBoolean(KEY_REQUIRE_BIOMETRIC_ON_OPEN, value)

    /** Ajustes > Privacidade — FLAG_SECURE no Android (components/ScreenshotGuard.android.kt); sem efeito no Desktop, não há API equivalente ali. */
    var blockScreenshots: Boolean
        get() = settings.getBoolean(KEY_BLOCK_SCREENSHOTS, true)
        set(value) = settings.putBoolean(KEY_BLOCK_SCREENSHOTS, value)
}

expect fun createPlatformSettings(): Settings
