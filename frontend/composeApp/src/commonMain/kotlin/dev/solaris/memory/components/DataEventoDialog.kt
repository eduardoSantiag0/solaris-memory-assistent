package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.solarisText
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * HANDOFF.md tela 04, campo "DATA DO EVENTO". Usa o DatePicker comum do
 * Material3 (disponível em código comum desde Compose Multiplatform
 * 1.6.10) para o calendário — é a única parte difícil de desenhar à mão
 * bem — e esteppers no mesmo estilo de DiasStepper para a hora, já que o
 * TimePicker padrão do Material (mostrador circular) contrasta demais com
 * o resto do tema retrô-plano.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEventoDialog(
    inicial: LocalDateTime?,
    onConfirmar: (LocalDateTime) -> Unit,
    onCancelar: () -> Unit,
) {
    val colors = LocalSolarisColors.current

    val millisIniciais = inicial?.date
        ?.let { LocalDateTime(it, kotlinx.datetime.LocalTime(0, 0)) }
        ?.toInstant(TimeZone.UTC)
        ?.toEpochMilliseconds()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = millisIniciais)

    var hora by remember { mutableStateOf(inicial?.hour ?: 12) }
    var minuto by remember { mutableStateOf(inicial?.let { (it.minute / 5) * 5 } ?: 0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.veu)
            .clickable(onClick = onCancelar),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .background(colors.fundo)
                .border(1.dp, colors.filete)
                .clickable(enabled = false) {}
                .padding(bottom = 20.dp),
        ) {
            DatePicker(state = datePickerState, showModeToggle = false, title = null, headline = null)

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NumeroStepper(valor = hora, min = 0, max = 23, onChange = { hora = it })
                Text(":", style = solarisText.tituloDialogo, color = colors.texto, modifier = Modifier.padding(horizontal = 8.dp))
                NumeroStepper(valor = minuto, min = 0, max = 55, passo = 5, onChange = { minuto = it }, formatar = { it.toString().padStart(2, '0') })
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                SecondaryButton(text = "Cancelar", onClick = onCancelar, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                PrimaryButton(
                    text = "Definir",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        val data = if (millis != null) {
                            Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                        } else {
                            Instant.fromEpochMilliseconds(0).toLocalDateTime(TimeZone.UTC).date
                        }
                        onConfirmar(LocalDateTime(data, kotlinx.datetime.LocalTime(hora, minuto)))
                    },
                )
            }
        }
    }
}

@Composable
private fun NumeroStepper(
    valor: Int,
    min: Int,
    max: Int,
    onChange: (Int) -> Unit,
    passo: Int = 1,
    formatar: (Int) -> String = { it.toString().padStart(2, '0') },
) {
    val colors = LocalSolarisColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(40.dp).border(1.dp, colors.filete).clickable { onChange(if (valor - passo < min) max else valor - passo) },
            contentAlignment = Alignment.Center,
        ) { Text("−", style = solarisText.corpo, color = colors.texto) }

        Text(
            formatar(valor),
            style = solarisText.corpo.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            color = colors.texto,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        Box(
            modifier = Modifier.size(40.dp).border(1.dp, colors.filete).clickable { onChange(if (valor + passo > max) min else valor + passo) },
            contentAlignment = Alignment.Center,
        ) { Text("+", style = solarisText.corpo, color = colors.texto) }
    }
}

@Composable
private fun HorizontalDivider() {
    val colors = LocalSolarisColors.current
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.filete))
}
