package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.solaris.memory.theme.LocalSolarisColors

enum class ChatRole { USER, ASSISTANT }

@Composable
fun RetroChatBubble(
    role: ChatRole,
    text: String,
    caption: String? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSolarisColors.current
    val isUser = role == ChatRole.USER
    val bubbleColor = if (isUser) colors.userBubble else colors.assistantBubble
    val onBubbleColor = if (isUser) colors.onUserBubble else colors.onAssistantBubble

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(bubbleColor)
                .border(2.dp, colors.outline)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(text, color = onBubbleColor, style = MaterialTheme.typography.bodyMedium)
            if (caption != null) {
                Text(caption, color = onBubbleColor.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp))
            }
        }
    }
}
