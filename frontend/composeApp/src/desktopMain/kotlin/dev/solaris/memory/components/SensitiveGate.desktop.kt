package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.solarisText
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Desktop has no biometric hardware — confirm with the account password instead (see SensitiveGate.kt). */
@Composable
actual fun rememberSensitiveGate(viewModel: AppViewModel): suspend () -> Boolean {
    var aoResolver by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }
    var senha by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf(false) }
    var verificando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (aoResolver != null) {
        SenhaDialog(
            senha = senha,
            onSenhaChange = { senha = it; erro = false },
            erro = erro,
            verificando = verificando,
            onConfirmar = {
                scope.launch {
                    verificando = true
                    val ok = viewModel.verificarSenha(senha)
                    verificando = false
                    if (ok) {
                        aoResolver?.invoke(true)
                        aoResolver = null
                        senha = ""
                    } else {
                        erro = true
                    }
                }
            },
            onCancelar = {
                aoResolver?.invoke(false)
                aoResolver = null
                senha = ""
                erro = false
            },
        )
    }

    return {
        suspendCancellableCoroutine { cont ->
            aoResolver = { resultado -> if (cont.isActive) cont.resume(resultado) }
        }
    }
}

@Composable
private fun SenhaDialog(
    senha: String,
    onSenhaChange: (String) -> Unit,
    erro: Boolean,
    verificando: Boolean,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    val colors = LocalSolarisColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.veu)
            .clickable(onClick = onCancelar),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .background(colors.fundo)
                .border(1.dp, colors.filete)
                .clickable(enabled = false) {}
                .padding(20.dp),
        ) {
            Text("Confirme para revelar", style = solarisText.tituloDialogo, color = colors.texto)
            Text(
                "Digite a senha da conta para mostrar este valor sensível.",
                style = solarisText.corpo,
                color = colors.texto,
                modifier = Modifier.padding(top = 12.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(colors.superficie)
                    .border(1.dp, if (erro) colors.acao else colors.filete)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                BasicTextField(
                    value = senha,
                    onValueChange = onSenhaChange,
                    textStyle = solarisText.corpo.copy(color = colors.texto),
                    cursorBrush = SolidColor(colors.texto),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (erro) {
                Text(
                    "Senha incorreta.",
                    style = solarisText.meta,
                    color = colors.acao,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                SecondaryButton(text = "Cancelar", onClick = onCancelar, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                PrimaryButton(
                    text = if (verificando) "Verificando..." else "Confirmar",
                    enabled = senha.isNotBlank() && !verificando,
                    onClick = onConfirmar,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
