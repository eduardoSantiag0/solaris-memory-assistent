package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.solarisText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val REVEAL_SECONDS = 30

/**
 * HANDOFF.md 5.4 "Dados sensíveis". Mask + 30s countdown + auto-rehide, plus
 * the identity gate before revealing (item 2) — Android BiometricPrompt or,
 * on Desktop (no biometric hardware), the account password. See
 * components/SensitiveGate.kt and its .android/.desktop actuals.
 *
 * v1.1 2.1: valor mascarado é superfície preenchida (`preenchimento`), sem
 * borda — não mais a caixa com filete da v1.
 */
@Composable
fun MaskedValue(value: String, viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val colors = LocalSolarisColors.current
    var revelado by remember { mutableStateOf(false) }
    var segundosRestantes by remember { mutableStateOf(REVEAL_SECONDS) }
    val autenticar = rememberSensitiveGate(viewModel)
    val scope = rememberCoroutineScope()

    LaunchedEffect(revelado) {
        if (!revelado) return@LaunchedEffect
        segundosRestantes = REVEAL_SECONDS
        while (segundosRestantes > 0) {
            delay(1000)
            segundosRestantes -= 1
        }
        revelado = false
    }

    Column(modifier = modifier.fillMaxWidth().background(colors.preenchimento)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (revelado) value else maskOf(value),
                style = solarisText.comando.copy(fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, letterSpacing = 0.5.sp),
                color = colors.texto,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.width(8.dp))
            OutlineChip(
                text = if (revelado) "Ocultar" else "Revelar",
                icon = SolarisIcon.REVELAR,
                onClick = {
                    if (revelado) {
                        revelado = false
                    } else {
                        scope.launch { if (autenticar()) revelado = true }
                    }
                },
            )
        }
        if (revelado) {
            val proporcao = segundosRestantes.toFloat() / REVEAL_SECONDS
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(colors.trilho)) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(proporcao.coerceIn(0f, 1f)).background(colors.acao))
            }
            Text(
                "OCULTA DE NOVO EM 0:${segundosRestantes.toString().padStart(2, '0')}",
                style = solarisText.meta,
                color = colors.texto,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        } else {
            Text(
                "SENSÍVEL · PEDE BIOMETRIA",
                style = solarisText.meta,
                color = colors.texto,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}

private fun maskOf(value: String): String = value.map { if (it.isLetterOrDigit()) '•' else it }.joinToString("")
