package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.SolarisIconGlyph
import dev.solaris.memory.theme.solarisText

private val FIELD_HEIGHT = 44.dp

/** HANDOFF.md 02-chat: "[+]" + campo + enviar, todos de 44dp. "Não há atalhos acima do campo." */
@Composable
fun Composer(
    value: String,
    onValueChange: (String) -> Unit,
    onNovaMemoria: () -> Unit,
    onEnviar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSolarisColors.current
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconSquareButton(SolarisIcon.NOVA, "Nova memória", onNovaMemoria)

        Box(
            modifier = Modifier
                .weight(1f)
                .height(FIELD_HEIGHT)
                .padding(horizontal = 8.dp)
                .background(colors.superficie)
                .border(1.dp, colors.filete)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text("Pergunte algo...", style = solarisText.corpo, color = colors.textoSecundario)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = solarisText.corpo.copy(color = colors.texto),
                cursorBrush = SolidColor(colors.texto),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Box(
            modifier = Modifier
                .size(FIELD_HEIGHT)
                .background(colors.acao)
                .clickable(onClick = onEnviar, onClickLabel = "Enviar"),
            contentAlignment = Alignment.Center,
        ) {
            SolarisIconGlyph(SolarisIcon.ENVIAR, colors.iconeSobreAcao)
        }
    }
}

/** Generic "campo" per design-tokens.json dimensoes.campoAltura (44dp). Used for search, server URL, login, forms. */
@Composable
fun SolarisTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    icon: SolarisIcon? = null,
    singleLine: Boolean = true,
) {
    val colors = LocalSolarisColors.current
    val heightModifier = if (singleLine) Modifier.height(FIELD_HEIGHT) else Modifier
    Row(
        modifier = modifier
            .then(heightModifier)
            .background(colors.superficie)
            .border(1.dp, colors.filete)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            SolarisIconGlyph(icon, colors.textoSecundario, size = 18.dp)
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        }
        Box(Modifier.weight(1f).padding(vertical = if (singleLine) 0.dp else 10.dp)) {
            if (value.isEmpty()) {
                Text(placeholder, style = solarisText.corpo, color = colors.textoSecundario)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = solarisText.corpo.copy(color = colors.texto),
                cursorBrush = SolidColor(colors.texto),
                singleLine = singleLine,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
