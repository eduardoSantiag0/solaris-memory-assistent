package dev.solaris.memory.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import dev.solaris.memory.resources.Res
import dev.solaris.memory.resources.ibm_plex_mono_medium
import dev.solaris.memory.resources.ibm_plex_mono_regular
import dev.solaris.memory.resources.ibm_plex_mono_semibold
import dev.solaris.memory.resources.ibm_plex_sans_medium
import dev.solaris.memory.resources.ibm_plex_sans_regular
import dev.solaris.memory.resources.ibm_plex_sans_semibold

/** design-tokens.json › tipografia. Weights beyond 400/500/600 aren't bundled — the handoff only specifies those three. */
@Composable
fun plexSans(): FontFamily = FontFamily(
    Font(Res.font.ibm_plex_sans_regular, FontWeight.Normal),
    Font(Res.font.ibm_plex_sans_medium, FontWeight.Medium),
    Font(Res.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
)

@Composable
fun plexMono(): FontFamily = FontFamily(
    Font(Res.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(Res.font.ibm_plex_mono_medium, FontWeight.Medium),
    Font(Res.font.ibm_plex_mono_semibold, FontWeight.SemiBold),
)

@Immutable
data class SolarisTextStyles(
    val tituloTela: TextStyle,
    val tituloDialogo: TextStyle,
    val tituloFolha: TextStyle,
    val corpo: TextStyle,
    val destaqueFicha: TextStyle,
    val comando: TextStyle,
    val meta: TextStyle,
    val navegacao: TextStyle,
    val navegacaoAtiva: TextStyle,
    val contador: TextStyle,
    val botao: TextStyle,
)

@Composable
fun solarisTextStyles(): SolarisTextStyles {
    val sans = plexSans()
    val mono = plexMono()
    return SolarisTextStyles(
        tituloTela = TextStyle(fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = 1.sp),
        tituloDialogo = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
        tituloFolha = TextStyle(fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
        corpo = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 1.45.em),
        destaqueFicha = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 21.sp, lineHeight = 1.35.em),
        comando = TextStyle(fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
        meta = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 12.sp, letterSpacing = 0.4.sp),
        navegacao = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 12.sp, letterSpacing = 1.sp, textAlign = TextAlign.Center),
        navegacaoAtiva = TextStyle(fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.sp, textAlign = TextAlign.Center),
        contador = TextStyle(fontFamily = mono, fontWeight = FontWeight.Medium, fontSize = 34.sp),
        botao = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    )
}
