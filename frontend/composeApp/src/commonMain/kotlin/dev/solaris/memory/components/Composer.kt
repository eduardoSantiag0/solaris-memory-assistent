package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.SolarisIconGlyph
import dev.solaris.memory.theme.solarisText

private val FIELD_HEIGHT = 44.dp

/**
 * HANDOFF.md 02-chat: "[+]" + campo + enviar, todos de 44dp. "Não há
 * atalhos de comando acima do campo." v1.1 2.1: "+" preenchido sem borda;
 * campo com só linha de base de 2dp, sem caixa.
 */
@Composable
fun Composer(
    value: String,
    onValueChange: (String) -> Unit,
    onNovaMemoria: () -> Unit,
    onEnviar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSolarisColors.current
    val baseColor = colors.texto
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        IconSquareButton(SolarisIcon.NOVA, "Nova memória", onNovaMemoria, preenchido = true)

        Box(
            modifier = Modifier
                .weight(1f)
                // Pedido do usuário em 15/10: o campo cresce pra baixo conforme o
                // texto quebra linha, até um teto (depois disso rola por dentro)
                // em vez de ficar travado numa linha só de 44dp.
                .heightIn(min = FIELD_HEIGHT, max = 160.dp)
                .padding(horizontal = 8.dp)
                .background(colors.superficie)
                .drawBehind {
                    drawLine(baseColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2.dp.toPx())
                }
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text("Mensagem aqui…", style = solarisText.corpo, color = colors.placeholder)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = solarisText.corpo.copy(color = colors.texto),
                cursorBrush = SolidColor(colors.texto),
                singleLine = false,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
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
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
) {
    val colors = LocalSolarisColors.current
    val baseColor = colors.texto
    val heightModifier = if (singleLine) Modifier.height(FIELD_HEIGHT) else Modifier
    Row(
        modifier = modifier
            .then(heightModifier)
            .background(colors.superficie)
            .drawBehind {
                drawLine(baseColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2.dp.toPx())
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            SolarisIconGlyph(icon, colors.textoSuave, size = 18.dp)
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        }
        Box(Modifier.weight(1f).padding(vertical = if (singleLine) 0.dp else 10.dp)) {
            if (value.isEmpty()) {
                Text(placeholder, style = solarisText.corpo, color = colors.placeholder)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = solarisText.corpo.copy(color = colors.texto),
                cursorBrush = SolidColor(colors.texto),
                singleLine = singleLine,
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
