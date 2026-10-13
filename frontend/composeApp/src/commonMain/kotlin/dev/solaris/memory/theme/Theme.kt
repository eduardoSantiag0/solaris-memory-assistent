package dev.solaris.memory.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

val LocalSolarisColors = compositionLocalOf { solarisColors(Palette.ESTACAO, dark = false) }
val LocalSolarisTextStyles = compositionLocalOf<SolarisTextStyles?> { null }

/** Sharp corners everywhere, including dialogs and sheets (design-tokens.json forma.cantos = 0). */
private val SolarisShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

enum class ThemeMode { CLARO, ESCURO, SISTEMA }

@Composable
fun SolarisTheme(palette: Palette, mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.CLARO -> false
        ThemeMode.ESCURO -> true
        ThemeMode.SISTEMA -> isSystemInDarkTheme()
    }
    val colors = solarisColors(palette, dark)
    val textStyles = solarisTextStyles()

    val materialColors = if (dark) {
        darkColorScheme(
            primary = colors.acao, secondary = colors.usuario,
            background = colors.fundo, surface = colors.superficie,
            onBackground = colors.texto, onSurface = colors.texto,
            error = colors.acao,
        )
    } else {
        lightColorScheme(
            primary = colors.acao, secondary = colors.usuario,
            background = colors.fundo, surface = colors.superficie,
            onBackground = colors.texto, onSurface = colors.texto,
            error = colors.acao,
        )
    }

    CompositionLocalProvider(LocalSolarisColors provides colors, LocalSolarisTextStyles provides textStyles) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = Typography(bodyLarge = textStyles.corpo),
            shapes = SolarisShapes,
            content = content,
        )
    }
}

/** Shorthand so screens don't repeat `LocalSolarisTextStyles.current!!` everywhere. */
val solarisText: SolarisTextStyles
    @Composable get() = LocalSolarisTextStyles.current ?: error("SolarisTheme not applied")
