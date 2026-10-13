package dev.solaris.memory.theme

import androidx.compose.ui.graphics.Color

/** Mirrors design/solaris-handoff/design-tokens.json — do not hand-tune values here, regenerate from that file if it changes. */
data class SolarisColors(
    val fundo: Color,
    val superficie: Color,
    val texto: Color,
    val textoSecundario: Color,
    val filete: Color,
    val usuario: Color,
    val textoSobreUsuario: Color,
    val acao: Color,
    val textoSobreAcao: Color,
    val iconeSobreAcao: Color,
    val status: Color,
    val foco: Color,
    val trilho: Color,
    val veu: Color,
)

enum class Palette(val id: String, val displayName: String, val subtitle: String, val official: Boolean) {
    ESTACAO("estacao", "Estação", "OFICIAL · PADRÃO", true),
    BIBLIOTECA("biblioteca", "Biblioteca", "AMEIXA E OCRE", false),
    GELO("gelo", "Gelo", "AZUL POLAR", false),
    DESERTO("deserto", "Deserto", "TERRA E ARGILA", false),
    CREPUSCULO("crepusculo", "Crepúsculo", "VIOLETA E LILÁS", false),
}

private fun c(hex: String) = Color(("FF" + hex.removePrefix("#")).toLong(16))
private fun c(hex: String, alphaHex: String) = Color((alphaHex + hex.removePrefix("#")).toLong(16))

private val DEFAULT_VEU_ESCURO = Color(0x9E000000)

fun solarisColors(palette: Palette, dark: Boolean): SolarisColors = when (palette) {
    Palette.ESTACAO -> if (!dark) SolarisColors(
        fundo = c("#E8E5D8"), superficie = c("#F3F1E8"), texto = c("#172621"), textoSecundario = c("#172621"),
        filete = c("#172621"), usuario = c("#B8A47B"), textoSobreUsuario = c("#172621"),
        acao = c("#995F4C"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#E8E5D8"),
        status = c("#566C5B"), foco = c("#995F4C"), trilho = c("#C9C6B8"), veu = c("#172621", "9E"),
    ) else SolarisColors(
        fundo = c("#172621"), superficie = c("#1F332C"), texto = c("#E8E5D8"), textoSecundario = c("#B8A47B"),
        filete = c("#566C5B"), usuario = c("#B8A47B"), textoSobreUsuario = c("#172621"),
        acao = c("#995F4C"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#E8E5D8"),
        status = c("#B8A47B"), foco = c("#B8A47B"), trilho = c("#566C5B"), veu = DEFAULT_VEU_ESCURO,
    )

    Palette.BIBLIOTECA -> if (!dark) SolarisColors(
        fundo = c("#EAE4D9"), superficie = c("#F4F0E8"), texto = c("#1E1B24"), textoSecundario = c("#1E1B24"),
        filete = c("#1E1B24"), usuario = c("#C49A55"), textoSobreUsuario = c("#1E1B24"),
        acao = c("#8C3F3F"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#EAE4D9"),
        status = c("#6B5A6E"), foco = c("#8C3F3F"), trilho = c("#C9C6B8"), veu = c("#1E1B24", "9E"),
    ) else SolarisColors(
        fundo = c("#1E1B24"), superficie = c("#2A2531"), texto = c("#EAE4D9"), textoSecundario = c("#C49A55"),
        filete = c("#6B5A6E"), usuario = c("#C49A55"), textoSobreUsuario = c("#1E1B24"),
        acao = c("#8C3F3F"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#EAE4D9"),
        status = c("#C49A55"), foco = c("#C49A55"), trilho = c("#6B5A6E"), veu = DEFAULT_VEU_ESCURO,
    )

    Palette.GELO -> if (!dark) SolarisColors(
        fundo = c("#E7EBEC"), superficie = c("#F3F5F5"), texto = c("#14202E"), textoSecundario = c("#14202E"),
        filete = c("#14202E"), usuario = c("#A9BFCF"), textoSobreUsuario = c("#14202E"),
        acao = c("#B5562E"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#E7EBEC"),
        status = c("#5E7387"), foco = c("#B5562E"), trilho = c("#C9C6B8"), veu = c("#14202E", "9E"),
    ) else SolarisColors(
        fundo = c("#14202E"), superficie = c("#1D2C3D"), texto = c("#E7EBEC"), textoSecundario = c("#A9BFCF"),
        filete = c("#5E7387"), usuario = c("#A9BFCF"), textoSobreUsuario = c("#14202E"),
        acao = c("#B5562E"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#E7EBEC"),
        status = c("#A9BFCF"), foco = c("#A9BFCF"), trilho = c("#5E7387"), veu = DEFAULT_VEU_ESCURO,
    )

    Palette.DESERTO -> if (!dark) SolarisColors(
        fundo = c("#ECE3D2"), superficie = c("#F5EFE3"), texto = c("#2A211B"), textoSecundario = c("#2A211B"),
        filete = c("#2A211B"), usuario = c("#C9946B"), textoSobreUsuario = c("#2A211B"),
        acao = c("#A24E32"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#ECE3D2"),
        status = c("#7A7F62"), foco = c("#A24E32"), trilho = c("#C9C6B8"), veu = c("#2A211B", "9E"),
    ) else SolarisColors(
        fundo = c("#2A211B"), superficie = c("#362B23"), texto = c("#ECE3D2"), textoSecundario = c("#C9946B"),
        filete = c("#7A7F62"), usuario = c("#C9946B"), textoSobreUsuario = c("#2A211B"),
        acao = c("#A24E32"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#ECE3D2"),
        status = c("#C9946B"), foco = c("#C9946B"), trilho = c("#7A7F62"), veu = DEFAULT_VEU_ESCURO,
    )

    Palette.CREPUSCULO -> if (!dark) SolarisColors(
        fundo = c("#ECE8F0"), superficie = c("#F6F3F8"), texto = c("#1F1A2E"), textoSecundario = c("#1F1A2E"),
        filete = c("#1F1A2E"), usuario = c("#B9A6D6"), textoSobreUsuario = c("#1F1A2E"),
        acao = c("#9B4F5C"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#ECE8F0"),
        status = c("#6F6488"), foco = c("#9B4F5C"), trilho = c("#C9C6B8"), veu = c("#1F1A2E", "9E"),
    ) else SolarisColors(
        fundo = c("#1F1A2E"), superficie = c("#2A2440"), texto = c("#ECE8F0"), textoSecundario = c("#B9A6D6"),
        filete = c("#6F6488"), usuario = c("#B9A6D6"), textoSobreUsuario = c("#1F1A2E"),
        acao = c("#9B4F5C"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#ECE8F0"),
        status = c("#B9A6D6"), foco = c("#B9A6D6"), trilho = c("#6F6488"), veu = DEFAULT_VEU_ESCURO,
    )
}

/** Swatch strip shown next to each palette option in Ajustes › Aparência (origem colors, light-mode order). */
fun paletteSwatch(palette: Palette): List<Color> = when (palette) {
    Palette.ESTACAO -> listOf(c("#172621"), c("#566C5B"), c("#E8E5D8"), c("#B8A47B"), c("#995F4C"))
    Palette.BIBLIOTECA -> listOf(c("#1E1B24"), c("#6B5A6E"), c("#EAE4D9"), c("#C49A55"), c("#8C3F3F"))
    Palette.GELO -> listOf(c("#14202E"), c("#5E7387"), c("#E7EBEC"), c("#A9BFCF"), c("#B5562E"))
    Palette.DESERTO -> listOf(c("#2A211B"), c("#7A7F62"), c("#ECE3D2"), c("#C9946B"), c("#A24E32"))
    Palette.CREPUSCULO -> listOf(c("#1F1A2E"), c("#6F6488"), c("#ECE8F0"), c("#B9A6D6"), c("#9B4F5C"))
}
