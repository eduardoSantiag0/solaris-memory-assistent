package dev.solaris.memory.theme

import androidx.compose.ui.graphics.Color

/**
 * Semantic color tokens for the "Colorful Retro Macintosh Pixel UI" (doc
 * section 11), with light and dark concrete palettes — the same pattern
 * used by the retronotes Obsidian theme (semantic variables like
 * `--background-primary`/`--text-normal`, redefined per mode): components
 * read from [SolarisColors], never a literal hex, so switching mode is one
 * value swap instead of touching every screen.
 */
data class SolarisColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onBackground: Color,
    val onSurface: Color,
    val outline: Color,
    val userBubble: Color,
    val onUserBubble: Color,
    val assistantBubble: Color,
    val onAssistantBubble: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val conversationBackground: Color,
    val desktopPattern: Color,
    val error: Color,
    val warning: Color,
)

// Light palette — straight from doc section 11.2.
val SolarisLightColors = SolarisColors(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFA5F2E2), // verde menta
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    outline = Color(0xFF000000),
    userBubble = Color(0xFFC59AF3), // lavanda
    onUserBubble = Color(0xFF000000),
    assistantBubble = Color(0xFFA5CFFF), // azul pastel
    onAssistantBubble = Color(0xFF000000),
    accentPrimary = Color(0xFFF17BAA), // rosa chiclete
    accentSecondary = Color(0xFFA5F2E2), // verde menta
    conversationBackground = Color(0xFFF1FFA2), // amarelo-lima
    desktopPattern = Color(0xFFBD643A), // laranja queimado
    error = Color(0xFFE85B5B),
    warning = Color(0xFFFFD36E),
)

// Dark palette — same hues, deepened for a dark surface (doc 11.4 wants an
// atmospheric, Solaris/Tarkovsky-leaning night mode, not just a contrast
// invert). Tune against real screens later; this is a first, defensible pass.
val SolarisDarkColors = SolarisColors(
    background = Color(0xFF16131F),
    surface = Color(0xFF211D2E),
    surfaceVariant = Color(0xFF2E4A45),
    onBackground = Color(0xFFECE9F5),
    onSurface = Color(0xFFECE9F5),
    outline = Color(0xFFECE9F5),
    userBubble = Color(0xFFA87BEF),
    onUserBubble = Color(0xFF16131F),
    assistantBubble = Color(0xFF6FA8F2),
    onAssistantBubble = Color(0xFF16131F),
    accentPrimary = Color(0xFFF15C9B),
    accentSecondary = Color(0xFF4FCDB5),
    conversationBackground = Color(0xFF241F33),
    desktopPattern = Color(0xFF9A4E2C),
    error = Color(0xFFE8625F),
    warning = Color(0xFFE0B454),
)
