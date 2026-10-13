package dev.solaris.memory.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.components.MemoryCard
import dev.solaris.memory.theme.LocalSolarisColors
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Composable
fun MemoriesScreen(viewModel: AppViewModel) {
    val colors = LocalSolarisColors.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.refreshMemories()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(12.dp),
    ) {
        Text("Memórias", style = MaterialTheme.typography.titleLarge, color = colors.onBackground)

        if (viewModel.memories.isEmpty()) {
            Text(
                "Nenhuma memória ainda. Use /add ou /add-temp no chat.",
                color = colors.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(viewModel.memories) { memory ->
                val isTemporary = memory.expiresAt != null
                val isExpired = memory.status == "EXPIRED" || memory.status == "DELETED"
                val remaining = remainingLabel(memory.expiresAt)

                MemoryCard(
                    content = memory.content,
                    isTemporary = isTemporary,
                    isExpired = isExpired,
                    timeRemainingLabel = remaining,
                    onDelete = { scope.launch { viewModel.deleteMemory(memory.id) } },
                )
            }
        }
    }
}

private fun remainingLabel(expiresAt: String?): String? {
    if (expiresAt == null) return null
    return try {
        val expiry = Instant.parse(expiresAt)
        val now = Clock.System.now()
        val remaining = expiry - now
        if (remaining.isNegative()) "vencida" else "restam ${remaining.inWholeHours}h"
    } catch (e: Exception) {
        null
    }
}
