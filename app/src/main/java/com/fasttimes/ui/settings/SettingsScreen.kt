package com.fasttimes.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fasttimes.data.Theme
import com.fasttimes.ui.theme.FastTimesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            ThemeSettingItem(
                selectedTheme = uiState.selectedTheme,
                onThemeChange = viewModel::onThemeChange
            )
        }
    }
}

@Composable
fun ThemeSettingItem(
    selectedTheme: Theme,
    onThemeChange: (Theme) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Theme", modifier = Modifier.weight(1f))

        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
            Text(selectedTheme.name.lowercase().replaceFirstChar { it.uppercase() })
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Theme.values().forEach { theme ->
                    DropdownMenuItem(
                        text = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            onThemeChange(theme)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FastTimesTheme {
        // This preview uses the default hiltViewModel, which doesn't work in previews.
        // The screen will render, but interactions might not work as expected.
        // For a more robust preview, a fake ViewModel could be provided.
        SettingsScreen(onNavigateUp = {})
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenDarkPreview() {
    FastTimesTheme(darkTheme = true) {
        SettingsScreen(onNavigateUp = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ThemeSettingItemPreview() {
    FastTimesTheme {
        ThemeSettingItem(selectedTheme = Theme.LIGHT, onThemeChange = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ThemeSettingItemDarkPreview() {
    FastTimesTheme(darkTheme = true) {
        ThemeSettingItem(selectedTheme = Theme.DARK, onThemeChange = {})
    }
}
