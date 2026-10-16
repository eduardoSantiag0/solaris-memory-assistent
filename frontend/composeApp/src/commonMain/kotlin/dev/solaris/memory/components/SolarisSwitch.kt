package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors

private val MIN_TOUCH_TARGET = 44.dp

/**
 * HANDOFF.md section 3: retângulo 40x22, desligado = fundo `superficie` +
 * pino `status`, ligado = fundo `texto` + pino `usuario`. O alvo de toque
 * real é 44dp (padrão do app) mesmo que a trilha visual continue 40x22 —
 * a trilha sozinha era menor que o mínimo de acessibilidade usado em
 * todo o resto do app.
 */
@Composable
fun SolarisSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    Box(
        modifier = modifier
            .size(MIN_TOUCH_TARGET)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 22.dp)
                .background(if (checked) colors.texto else colors.superficie)
                .border(1.dp, colors.filete),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = if (checked) 20.dp else 2.dp)
                    .size(16.dp)
                    .background(if (checked) colors.usuario else colors.status),
            )
        }
    }
}
