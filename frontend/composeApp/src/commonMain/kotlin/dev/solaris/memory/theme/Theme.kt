package dev.solaris.memory.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LocalSolarisColors = compositionLocalOf { SolarisLightColors }

/**
 * Square-ish corners, bold weights: the doc explicitly rules out
 * glassmorphism, blur and "cantos exageradamente arredondados" (11.1). A
 * real bitmap/pixel font is a follow-up (needs a font asset); bold system
 * fonts get the retro silhouette close enough for this first pass.
 */
private val SolarisShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(2.dp),
    large = RoundedCornerShape(2.dp),
    extraLarge = RoundedCornerShape(4.dp),
)

private val SolarisTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = 0.5.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 0.5.sp),
)

@Composable
fun SolarisTheme(darkMode: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkMode) SolarisDarkColors else SolarisLightColors
    val materialColors = if (darkMode) {
        androidx.compose.material3.darkColorScheme(
            primary = colors.accentPrimary,
            secondary = colors.accentSecondary,
            background = colors.background,
            surface = colors.surface,
            onBackground = colors.onBackground,
            onSurface = colors.onSurface,
            error = colors.error,
        )
    } else {
        androidx.compose.material3.lightColorScheme(
            primary = colors.accentPrimary,
            secondary = colors.accentSecondary,
            background = colors.background,
            surface = colors.surface,
            onBackground = colors.onBackground,
            onSurface = colors.onSurface,
            error = colors.error,
        )
    }

    CompositionLocalProvider(LocalSolarisColors provides colors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = SolarisTypography,
            shapes = SolarisShapes,
            content = content,
        )
    }
}
