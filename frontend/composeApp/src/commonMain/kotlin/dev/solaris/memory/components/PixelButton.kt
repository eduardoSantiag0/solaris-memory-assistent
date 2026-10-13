package dev.solaris.memory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.solaris.memory.theme.LocalSolarisColors

/**
 * A rigid, offset drop shadow (never blurred) stands in for the "sombras
 * rígidas e deslocadas" the doc calls for (section 11.1) — two stacked
 * boxes, no elevation API, no blur.
 */
@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    contentColor: Color = Color.Black,
) {
    val colors = LocalSolarisColors.current
    val bg = backgroundColor ?: colors.accentPrimary

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .offset(x = 4.dp, y = 4.dp)
                .background(colors.outline)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text, color = Color.Transparent, style = MaterialTheme.typography.labelLarge)
        }
        Box(
            modifier = Modifier
                .clickable(onClick = onClick)
                .background(bg)
                .border(2.dp, colors.outline)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text, color = contentColor, style = MaterialTheme.typography.labelLarge)
        }
    }
}
