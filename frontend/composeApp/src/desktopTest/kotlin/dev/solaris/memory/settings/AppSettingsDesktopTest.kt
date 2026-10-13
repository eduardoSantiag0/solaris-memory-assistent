package dev.solaris.memory.settings

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Exercises the exact production path (createPlatformSettings -> PreferencesSettings
 * -> java.util.prefs) without any UI, since simulating real clicks against the
 * running desktop window turned out to be unreliable in this environment (it can
 * land on whatever window the OS actually has focused instead).
 */
class AppSettingsDesktopTest {

    @AfterTest
    fun resetToDefaults() {
        val settings = AppSettings(createPlatformSettings())
        settings.darkMode = false
        settings.token = null
    }

    @Test
    fun darkModePreferencePersistsAcrossInstances() {
        val first = AppSettings(createPlatformSettings())
        first.darkMode = true

        val second = AppSettings(createPlatformSettings())
        assertEquals(true, second.darkMode)
    }

    @Test
    fun tokenRoundTripsAndClears() {
        val settings = AppSettings(createPlatformSettings())
        assertNull(settings.token)

        settings.token = "fake-jwt-for-test"
        assertEquals("fake-jwt-for-test", AppSettings(createPlatformSettings()).token)

        settings.token = null
        assertNull(AppSettings(createPlatformSettings()).token)
    }

    @Test
    fun serverUrlDefaultsWhenNeverSet() {
        val settings = AppSettings(createPlatformSettings())
        // Whatever it is, it must never be blank - the app always needs a base URL to call.
        assert(settings.serverUrl.isNotBlank())
    }
}
