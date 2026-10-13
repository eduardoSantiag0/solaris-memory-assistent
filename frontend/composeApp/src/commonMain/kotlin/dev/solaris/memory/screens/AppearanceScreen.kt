package dev.solaris.memory.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import dev.solaris.memory.AppViewModel
import dev.solaris.memory.components.OutlineChip
import dev.solaris.memory.components.dashedBorder
import dev.solaris.memory.theme.LocalSolarisColors
import dev.solaris.memory.theme.Palette
import dev.solaris.memory.theme.SolarisIcon
import dev.solaris.memory.theme.ThemeMode
import dev.solaris.memory.theme.paletteSwatch
import dev.solaris.memory.theme.solarisText

/** HANDOFF.md tela 09 — Aparência: TEMA (segmentado), PALETA (5 opções), ESTILOS IMPORTADOS (seção 7, não implementada ainda). */
@Composable
fun AppearanceScreen(viewModel: AppViewModel) {
    val colors = LocalSolarisColors.current

    Column(modifier = Modifier.fillMaxSize().background(colors.fundo).padding(16.dp)) {
        Text("TEMA", style = solarisText.meta, color = colors.textoSecundario)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).border(1.dp, colors.filete)) {
            SegmentoTema("Claro", viewModel.themeMode == ThemeMode.CLARO) { viewModel.updateThemeMode(ThemeMode.CLARO) }
            SegmentoTema("Escuro", viewModel.themeMode == ThemeMode.ESCURO) { viewModel.updateThemeMode(ThemeMode.ESCURO) }
            SegmentoTema("Sistema", viewModel.themeMode == ThemeMode.SISTEMA) { viewModel.updateThemeMode(ThemeMode.SISTEMA) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("PALETA", style = solarisText.meta, color = colors.textoSecundario)
            Text("OFICIAL: ESTAÇÃO", style = solarisText.meta, color = colors.textoSecundario)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (palette in Palette.entries) {
                PaletteOption(palette, selected = viewModel.palette == palette, onClick = { viewModel.updatePalette(palette) })
            }
        }

        Text("ESTILOS IMPORTADOS", style = solarisText.meta, color = colors.textoSecundario, modifier = Modifier.padding(top = 28.dp, bottom = 8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .dashedBorder(colors.filete)
                .padding(16.dp),
        ) {
            Text("Nenhum estilo importado ainda.", style = solarisText.corpo, color = colors.texto)
            Text(
                "Um arquivo .json com as cinco cores (base, linha, papel, destaque e ação). O app confere o contraste antes de aplicar.",
                style = solarisText.corpo,
                color = colors.textoSecundario,
                modifier = Modifier.padding(top = 8.dp),
            )
            OutlineChip(
                text = "Importar estilo",
                icon = SolarisIcon.NOVA,
                onClick = { /* TODO: file picker + validação de contraste (HANDOFF.md seção 7) */ },
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun RowScope.SegmentoTema(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalSolarisColors.current
    Box(
        modifier = Modifier
            .weight(1f)
            .background(if (selected) colors.usuario else colors.fundo)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = if (selected) solarisText.navegacaoAtiva.copy(fontSize = solarisText.corpo.fontSize) else solarisText.corpo, color = colors.texto)
    }
}

@Composable
private fun PaletteOption(palette: Palette, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalSolarisColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.filete)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(1.dp, colors.filete, CircleShape)
                .let { if (selected) it.background(colors.texto) else it },
        )
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(palette.displayName, style = solarisText.corpo, color = colors.texto)
            Text(palette.subtitle, style = solarisText.meta, color = colors.textoSecundario)
        }
        Row {
            for (swatch in paletteSwatch(palette)) {
                Box(modifier = Modifier.size(width = 10.dp, height = 20.dp).background(swatch))
            }
        }
    }
}
