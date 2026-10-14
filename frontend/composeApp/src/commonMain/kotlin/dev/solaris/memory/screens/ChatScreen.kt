package dev.solaris.memory.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.ChatRole
import dev.solaris.memory.components.AssistantMessageBody
import dev.solaris.memory.components.AssistantMessageMeta
import dev.solaris.memory.components.Composer
import dev.solaris.memory.components.MaskedValue
import dev.solaris.memory.components.NovaMemoriaSheet
import dev.solaris.memory.components.UserMessage
import dev.solaris.memory.theme.LocalSolarisColors
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: AppViewModel) {
    val colors = LocalSolarisColors.current
    var input by remember { mutableStateOf("") }
    var mostrarNovaMemoria by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.chatHistory.size) {
        if (viewModel.chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.chatHistory.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.fundo)
                .padding(16.dp),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(viewModel.chatHistory) { entry ->
                    when (entry.role) {
                        ChatRole.USER -> UserMessage(entry.text)
                        ChatRole.ASSISTANT -> Column {
                            AssistantMessageMeta(entry.horario)
                            if (entry.sensivel) {
                                MaskedValue(entry.text, modifier = Modifier.padding(top = 2.dp))
                            } else {
                                AssistantMessageBody(entry.text, modifier = Modifier.padding(top = 2.dp))
                            }
                            if (entry.caption != null) {
                                AssistantMessageMeta(entry.caption, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }

            Composer(
                value = input,
                onValueChange = { input = it },
                onNovaMemoria = { mostrarNovaMemoria = true },
                onEnviar = {
                    val toSend = input
                    input = ""
                    scope.launch { viewModel.sendChatInput(toSend) }
                },
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        if (mostrarNovaMemoria) {
            NovaMemoriaSheet(
                onDismiss = { mostrarNovaMemoria = false },
                onSalvar = { content, ttlDays, sensitive ->
                    viewModel.createMemoryFromForm(content, ttlDays, null, sensitive)
                },
            )
        }
    }
}
