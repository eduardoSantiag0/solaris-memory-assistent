package dev.solaris.memory.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
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
import dev.solaris.memory.components.PixelButton
import dev.solaris.memory.components.RetroTextField
import dev.solaris.memory.theme.LocalSolarisColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val colors = LocalSolarisColors.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
    ) {
        Text("Ajustes", style = MaterialTheme.typography.titleLarge, color = colors.onBackground)

        Text(
            "Tema visual",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onBackground,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
        )
        Row {
            PixelButton(
                text = "Claro",
                onClick = { viewModel.updateDarkMode(false) },
                backgroundColor = if (!viewModel.darkMode) colors.accentPrimary else colors.surfaceVariant,
            )
            PixelButton(
                text = "Escuro",
                onClick = { viewModel.updateDarkMode(true) },
                backgroundColor = if (viewModel.darkMode) colors.accentPrimary else colors.surfaceVariant,
                modifier = Modifier.padding(start = 12.dp),
            )
        }

        Text(
            "Servidor",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onBackground,
            modifier = Modifier.padding(top = 28.dp, bottom = 8.dp),
        )
        Row {
            RetroTextField(
                value = viewModel.serverUrl,
                onValueChange = { viewModel.serverUrl = it },
                placeholder = "http://servidor:8080",
                modifier = Modifier.weight(1f),
            )
            if (viewModel.isLoggedIn) {
                PixelButton(
                    text = "Aplicar",
                    onClick = { viewModel.applyServerUrl() },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        if (!viewModel.isLoggedIn) {
            var username by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

            Text(
                "Login",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onBackground,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
            )
            RetroTextField(value = username, onValueChange = { username = it }, placeholder = "usuário", modifier = Modifier.fillMaxWidth())
            RetroTextField(value = password, onValueChange = { password = it }, placeholder = "senha", modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

            viewModel.loginError?.let {
                Text(it, color = colors.error, modifier = Modifier.padding(top = 8.dp))
            }

            PixelButton(
                text = "Entrar",
                onClick = { scope.launch { viewModel.login(username, password) } },
                modifier = Modifier.padding(top = 12.dp),
            )
        } else {
            PixelButton(
                text = "Sair",
                onClick = { viewModel.logout() },
                backgroundColor = colors.error,
                modifier = Modifier.padding(top = 20.dp),
            )
        }
    }
}
