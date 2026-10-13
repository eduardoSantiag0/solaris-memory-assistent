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
import dev.solaris.memory.components.PixelButton
import dev.solaris.memory.components.RetroChatBubble
import dev.solaris.memory.components.RetroTextField
import dev.solaris.memory.theme.LocalSolarisColors
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: AppViewModel) {
    val colors = LocalSolarisColors.current
    var input by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.chatHistory.size) {
        if (viewModel.chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.chatHistory.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.conversationBackground)
            .padding(12.dp),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(viewModel.chatHistory) { entry ->
                RetroChatBubble(role = entry.role, text = entry.text, caption = entry.caption)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            RetroTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = "/add, /add-temp <dias> ou pergunte algo...",
                modifier = Modifier.weight(1f),
            )
            PixelButton(
                text = "Enviar",
                onClick = {
                    val toSend = input
                    input = ""
                    scope.launch { viewModel.sendChatInput(toSend) }
                },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
