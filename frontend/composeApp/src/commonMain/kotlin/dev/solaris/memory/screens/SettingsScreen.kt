package dev.solaris.memory.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.components.OutlineChip
import dev.solaris.memory.components.PrimaryButton
import dev.solaris.memory.components.SecondaryButton
import dev.solaris.memory.components.SolarisSwitch
import dev.solaris.memory.components.SolarisTextField
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.solarisText
import kotlinx.coroutines.launch

/** HANDOFF.md tela 08 — Ajustes: CONEXÃO, APARÊNCIA (leva à tela 09), PRIVACIDADE, ações. */
@Composable
fun SettingsScreen(viewModel: AppViewModel, onAbrirAparencia: () -> Unit) {
    val colors = LocalSolarisColors.current

    if (!viewModel.isLoggedIn) {
        LoginForm(viewModel)
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.fundo).padding(16.dp)) {
        SecaoTitulo("CONEXÃO")
        Text("VPN · WireGuard", style = solarisText.corpo, color = colors.texto, modifier = Modifier.padding(top = 8.dp))
        Text("Servidor (API)", style = solarisText.corpo, color = colors.texto, modifier = Modifier.padding(top = 4.dp))
        Row(modifier = Modifier.padding(top = 8.dp)) {
            SolarisTextField(
                value = viewModel.serverUrl,
                onValueChange = { viewModel.serverUrl = it },
                placeholder = "http://servidor:8080",
                modifier = Modifier.weight(1f),
            )
            OutlineChip(text = "Testar", onClick = { viewModel.applyServerUrl() }, modifier = Modifier.padding(start = 8.dp))
        }

        SecaoTitulo("APARÊNCIA", topPadding = 28.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAbrirAparencia)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Tema e paleta", style = solarisText.corpo, color = colors.texto)
            Text(viewModel.palette.displayName, style = solarisText.meta, color = colors.textoSecundario)
        }

        SecaoTitulo("PRIVACIDADE", topPadding = 28.dp)
        val privacidade = remember {
            mutableStateOf(
                listOf(
                    "Pedir biometria ao abrir" to true,
                    "Mascarar dados sensíveis" to true,
                    "Ocultar de novo depois de 30 segundos" to true,
                    "Bloquear capturas de tela" to true,
                    "Mostrar texto nas notificações" to false,
                ),
            )
        }
        privacidade.value.forEachIndexed { index, (label, checked) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(label, style = solarisText.corpo, color = colors.texto, modifier = Modifier.weight(1f))
                SolarisSwitch(
                    checked = checked,
                    onCheckedChange = { newValue ->
                        privacidade.value = privacidade.value.toMutableList().also { it[index] = label to newValue }
                    },
                )
            }
        }

        Spacer(Modifier.weight(1f))
        SecondaryButton(text = "Limpar cache", onClick = {})
        SecondaryButton(text = "Encerrar sessão", onClick = { viewModel.logout() }, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun SecaoTitulo(text: String, topPadding: androidx.compose.ui.unit.Dp = 16.dp) {
    val colors = LocalSolarisColors.current
    Text(text, style = solarisText.meta, color = colors.textoSecundario, modifier = Modifier.padding(top = topPadding, bottom = 8.dp))
}

@Composable
private fun LoginForm(viewModel: AppViewModel) {
    val colors = LocalSolarisColors.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(colors.fundo).padding(16.dp)) {
        SecaoTitulo("SERVIDOR")
        SolarisTextField(
            value = viewModel.serverUrl,
            onValueChange = { viewModel.serverUrl = it },
            placeholder = "http://servidor:8080",
            modifier = Modifier.fillMaxWidth(),
        )

        SecaoTitulo("LOGIN", topPadding = 24.dp)
        SolarisTextField(value = username, onValueChange = { username = it }, placeholder = "usuário", modifier = Modifier.fillMaxWidth())
        SolarisTextField(
            value = password, onValueChange = { password = it }, placeholder = "senha",
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )

        viewModel.loginError?.let {
            Text(it, color = colors.acao, style = solarisText.corpo, modifier = Modifier.padding(top = 8.dp))
        }

        PrimaryButton(
            text = "Entrar",
            onClick = { scope.launch { viewModel.login(username, password) } },
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
