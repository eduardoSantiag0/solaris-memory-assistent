package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.solaris.memory.theme.LocalSolarisColors

/**
 * Temporary vs. permanent is shown by text + icon glyph, never only by
 * color (doc 11.5 — accessibility requirement, not decorative choice).
 */
@Composable
fun MemoryCard(
    content: String,
    isTemporary: Boolean,
    isExpired: Boolean,
    timeRemainingLabel: String?,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSolarisColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .border(2.dp, colors.outline)
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val typeLabel = when {
                isExpired -> "⌛ VENCIDA"
                isTemporary -> "⏳ TEMPORÁRIA"
                else -> "📌 PERMANENTE"
            }
            Text(typeLabel, style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp), color = colors.onSurface)
            if (isTemporary && timeRemainingLabel != null && !isExpired) {
                Text(timeRemainingLabel, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp), color = colors.onSurface)
            }
        }
        Text(
            content,
            color = colors.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            PixelButton(text = "Excluir", onClick = onDelete, backgroundColor = colors.error)
        }
    }
}
