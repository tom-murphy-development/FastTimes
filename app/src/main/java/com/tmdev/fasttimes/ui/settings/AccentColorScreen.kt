/*
 * Copyright (C) 2025 tom-murphy-development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tmdev.fasttimes.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmdev.fasttimes.ui.theme.FastTimesPreviewTheme
import com.tmdev.fasttimes.ui.theme.accentColors
import com.tmdev.fasttimes.ui.theme.seedColors

@Composable
fun AccentColorScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AccentColorScreenContent(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onUseSystemColorsChanged = viewModel::onUseSystemColorsChanged,
        onUseExpressiveThemeChanged = viewModel::onUseExpressiveThemeChanged,
        onSeedColorChanged = viewModel::onSeedColorChanged,
        onAccentColorChanged = viewModel::onAccentColorChanged
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AccentColorScreenContent(
    uiState: SettingsUiState,
    onNavigateUp: () -> Unit,
    onUseSystemColorsChanged: (Boolean) -> Unit,
    onUseExpressiveThemeChanged: (Boolean) -> Unit,
    onSeedColorChanged: (Long) -> Unit,
    onAccentColorChanged: (Long) -> Unit
) {
    val dynamicAccentColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary) + accentColors

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Theme & Accent Color") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Use System Colors")
                    Switch(
                        checked = uiState.useSystemColors,
                        onCheckedChange = onUseSystemColorsChanged
                    )
                }
                AnimatedVisibility(visible = uiState.useSystemColors) {
                    Text(
                        text = "Disable to select a custom theme color.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Use Expressive Theme",
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (uiState.useSystemColors) 0.38f else 1.0f
                    )
                )
                Switch(
                    checked = uiState.useExpressiveTheme,
                    onCheckedChange = onUseExpressiveThemeChanged,
                    enabled = !uiState.useSystemColors
                )
            }

            AnimatedVisibility(visible = !uiState.useSystemColors) {
                Column {
                    Text(
                        "Base Color",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        seedColors.forEach { color ->
                            val isItemSelected = uiState.seedColor == color.toArgb().toLong()
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(color, shape = CircleShape)
                                    .clickable { onSeedColorChanged(color.toArgb().toLong()) }
                                    .border(
                                        width = if (isItemSelected) 4.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isItemSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                "Accent Color",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                dynamicAccentColors.forEach { color ->
                    val isItemSelected = uiState.accentColor == color.toArgb().toLong()
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(color, shape = CircleShape)
                            .clickable { onAccentColorChanged(color.toArgb().toLong()) }
                            .border(
                                width = if (isItemSelected) 4.dp else 0.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isItemSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AccentColorScreenPreview() {
    FastTimesPreviewTheme {
        AccentColorScreenContent(
            uiState = SettingsUiState(),
            onNavigateUp = {},
            onUseSystemColorsChanged = {},
            onUseExpressiveThemeChanged = {},
            onSeedColorChanged = {},
            onAccentColorChanged = {}
        )
    }
}
