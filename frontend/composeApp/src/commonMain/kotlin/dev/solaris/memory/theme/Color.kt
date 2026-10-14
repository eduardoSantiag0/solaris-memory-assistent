package dev.solaris.memory.theme

import androidx.compose.ui.graphics.Color

/** Mirrors design/solaris-handoff_v2/solaris-handoff_2/design-tokens.json — do not hand-tune values here, regenerate from that file if it changes. */
data class SolarisColors(
    /** Whether this is the dark variant — needed by the handful of components whose color choice differs by theme, not just by token (e.g. SavedMessage's flag: acao no claro, usuario no escuro). */
    val dark: Boolean,
    val fundo: Color,
    val superficie: Color,
    val texto: Color,
    val textoSecundario: Color,
    val textoSuave: Color,
    val placeholder: Color,
    val filete: Color,
    val divisoria: Color,
    val tracejado: Color,
    val preenchimento: Color,
    val seloTemporaria: Color,
    val seloPermanente: Color,
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

/** Parses the design tokens' `#RRGGBBAA` notation (alpha as the trailing byte), unlike `c(hex, alphaHex)` above. */
private fun cAlpha(hex8: String): Color {
    val clean = hex8.removePrefix("#")
    return c(clean.substring(0, 6), clean.substring(6, 8))
}

private val DEFAULT_VEU_ESCURO = Color(0x9E000000)

fun solarisColors(palette: Palette, dark: Boolean): SolarisColors = when (palette) {
    Palette.ESTACAO -> if (!dark) SolarisColors(
        dark = false,
        fundo = c("#E8E5D8"), superficie = c("#F3F1E8"), texto = c("#172621"), textoSecundario = c("#172621"),
        textoSuave = c("#3F5045"), placeholder = cAlpha("#1726216B"),
        filete = c("#172621"), divisoria = c("#CFCBBE"), tracejado = c("#A9A596"), preenchimento = c("#DDD9CB"),
        seloTemporaria = c("#EEDCD3"), seloPermanente = c("#DCE2D8"),
        usuario = c("#B8A47B"), textoSobreUsuario = c("#172621"),
        acao = c("#995F4C"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#E8E5D8"),
        status = c("#566C5B"), foco = c("#995F4C"), trilho = c("#C9C6B8"), veu = c("#172621", "9E"),
    ) else SolarisColors(
        dark = true,
        fundo = c("#172621"), superficie = c("#1F332C"), texto = c("#E8E5D8"), textoSecundario = c("#B8A47B"),
        textoSuave = c("#C3C6BB"), placeholder = cAlpha("#E8E5D86B"),
        filete = c("#566C5B"), divisoria = c("#2E4740"), tracejado = c("#4F675C"), preenchimento = c("#24382F"),
        seloTemporaria = c("#3B2B26"), seloPermanente = c("#22382E"),
        usuario = c("#B8A47B"), textoSobreUsuario = c("#172621"),
        acao = c("#995F4C"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#E8E5D8"),
        status = c("#B8A47B"), foco = c("#B8A47B"), trilho = c("#566C5B"), veu = DEFAULT_VEU_ESCURO,
    )

    Palette.BIBLIOTECA -> if (!dark) SolarisColors(
        dark = false,
        fundo = c("#EAE4D9"), superficie = c("#F4F0E8"), texto = c("#1E1B24"), textoSecundario = c("#1E1B24"),
        textoSuave = c("#4C4150"), placeholder = cAlpha("#1E1B246B"),
        filete = c("#1E1B24"), divisoria = c("#D2CCC3"), tracejado = c("#ADA8A3"), preenchimento = c("#DCD6CC"),
        seloTemporaria = c("#D9C6BD"), seloPermanente = c("#D8D1CA"),
        usuario = c("#C49A55"), textoSobreUsuario = c("#1E1B24"),
        acao = c("#8C3F3F"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#EAE4D9"),
        status = c("#6B5A6E"), foco = c("#8C3F3F"), trilho = c("#C9C6B8"), veu = c("#1E1B24", "9E"),
    ) else SolarisColors(
        dark = true,
        fundo = c("#1E1B24"), superficie = c("#2A2531"), texto = c("#EAE4D9"), textoSecundario = c("#C49A55"),
        textoSuave = c("#C5C0B8"), placeholder = cAlpha("#EAE4D96B"),
        filete = c("#6B5A6E"), divisoria = c("#39313E"), tracejado = c("#544758"), preenchimento = c("#2C2931"),
        seloTemporaria = c("#36232A"), seloPermanente = c("#2D2833"),
        usuario = c("#C49A55"), textoSobreUsuario = c("#1E1B24"),
        acao = c("#8C3F3F"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#EAE4D9"),
        status = c("#C49A55"), foco = c("#C49A55"), trilho = c("#6B5A6E"), veu = DEFAULT_VEU_ESCURO,
    )

    Palette.GELO -> if (!dark) SolarisColors(
        dark = false,
        fundo = c("#E7EBEC"), superficie = c("#F3F5F5"), texto = c("#14202E"), textoSecundario = c("#14202E"),
        textoSuave = c("#405263"), placeholder = cAlpha("#14202E6B"),
        filete = c("#14202E"), divisoria = c("#CED3D5"), tracejado = c("#A8AEB3"), preenchimento = c("#D8DDDF"),
        seloTemporaria = c("#DED0CA"), seloPermanente = c("#D4DADE"),
        usuario = c("#A9BFCF"), textoSobreUsuario = c("#14202E"),
        acao = c("#B5562E"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#E7EBEC"),
        status = c("#5E7387"), foco = c("#B5562E"), trilho = c("#C9C6B8"), veu = c("#14202E", "9E"),
    ) else SolarisColors(
        dark = true,
        fundo = c("#14202E"), superficie = c("#1D2C3D"), texto = c("#E7EBEC"), textoSecundario = c("#A9BFCF"),
        textoSuave = c("#C1C6CA"), placeholder = cAlpha("#E7EBEC6B"),
        filete = c("#5E7387"), divisoria = c("#2E3D4D"), tracejado = c("#485A6C"), preenchimento = c("#232E3B"),
        seloTemporaria = c("#372C2E"), seloPermanente = c("#233140"),
        usuario = c("#A9BFCF"), textoSobreUsuario = c("#14202E"),
        acao = c("#B5562E"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#E7EBEC"),
        status = c("#A9BFCF"), foco = c("#A9BFCF"), trilho = c("#5E7387"), veu = DEFAULT_VEU_ESCURO,
    )

    Palette.DESERTO -> if (!dark) SolarisColors(
        dark = false,
        fundo = c("#ECE3D2"), superficie = c("#F5EFE3"), texto = c("#2A211B"), textoSecundario = c("#2A211B"),
        textoSuave = c("#5A5946"), placeholder = cAlpha("#2A211B6B"),
        filete = c("#2A211B"), divisoria = c("#D5CCBC"), tracejado = c("#B2A99B"), preenchimento = c("#DED5C5"),
        seloTemporaria = c("#DFC8B5"), seloPermanente = c("#DCD5C2"),
        usuario = c("#C9946B"), textoSobreUsuario = c("#2A211B"),
        acao = c("#A24E32"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#ECE3D2"),
        status = c("#7A7F62"), foco = c("#A24E32"), trilho = c("#C9C6B8"), veu = c("#2A211B", "9E"),
    ) else SolarisColors(
        dark = true,
        fundo = c("#2A211B"), superficie = c("#362B23"), texto = c("#ECE3D2"), textoSecundario = c("#C9946B"),
        textoSuave = c("#C9C0B1"), placeholder = cAlpha("#ECE3D26B"),
        filete = c("#7A7F62"), divisoria = c("#464234"), tracejado = c("#62634D"), preenchimento = c("#382F28"),
        seloTemporaria = c("#442B20"), seloPermanente = c("#3A3429"),
        usuario = c("#C9946B"), textoSobreUsuario = c("#2A211B"),
        acao = c("#A24E32"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#ECE3D2"),
        status = c("#C9946B"), foco = c("#C9946B"), trilho = c("#7A7F62"), veu = DEFAULT_VEU_ESCURO,
    )

    Palette.CREPUSCULO -> if (!dark) SolarisColors(
        dark = false,
        fundo = c("#ECE8F0"), superficie = c("#F6F3F8"), texto = c("#1F1A2E"), textoSecundario = c("#1F1A2E"),
        textoSuave = c("#4F4664"), placeholder = cAlpha("#1F1A2E6B"),
        filete = c("#1F1A2E"), divisoria = c("#D3CFD9"), tracejado = c("#AEAAB6"), preenchimento = c("#DEDAE2"),
        seloTemporaria = c("#DDCCD5"), seloPermanente = c("#DAD6E1"),
        usuario = c("#B9A6D6"), textoSobreUsuario = c("#1F1A2E"),
        acao = c("#9B4F5C"), textoSobreAcao = c("#FFFFFF"), iconeSobreAcao = c("#ECE8F0"),
        status = c("#6F6488"), foco = c("#9B4F5C"), trilho = c("#C9C6B8"), veu = c("#1F1A2E", "9E"),
    ) else SolarisColors(
        dark = true,
        fundo = c("#1F1A2E"), superficie = c("#2A2440"), texto = c("#ECE8F0"), textoSecundario = c("#B9A6D6"),
        textoSuave = c("#C7C3CD"), placeholder = cAlpha("#ECE8F06B"),
        filete = c("#6F6488"), divisoria = c("#3B344E"), tracejado = c("#574E6D"), preenchimento = c("#2D283C"),
        seloTemporaria = c("#3A2638"), seloPermanente = c("#2F2940"),
        usuario = c("#B9A6D6"), textoSobreUsuario = c("#1F1A2E"),
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
