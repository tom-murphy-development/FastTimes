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
package com.fasttimes.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fasttimes.data.AppTheme
import com.fasttimes.ui.theme.FastTimesTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Suppress("UNUSED_ASSIGNMENT")
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAccentColorClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPermissionRationale by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var permissionAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showImportConfirmation by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var uriForImport by remember { mutableStateOf<Uri?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                permissionAction?.invoke()
                permissionAction = null
            }
        }
    )

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri -> uri?.let { viewModel.onExportData(it) } }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                uriForImport = uri
                showImportConfirmation = true
            }
        }
    )

    fun handlePermission(onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onGranted()
            } else {
                permissionAction = onGranted
                showPermissionRationale = true
            }
        } else {
            onGranted()
        }
    }

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsScreenEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = { showImportConfirmation = false },
            title = { Text("Import Data") },
            text = { Text("This will replace all your existing data. Are you sure?") },
            confirmButton = {
                Button(onClick = {
                    uriForImport?.let { viewModel.onImportData(it) }
                    showImportConfirmation = false
                }) {
                    Text("Import")
                }
            },
            dismissButton = {
                Button(onClick = { showImportConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Permission Required") },
            text = { Text("To ensure you're notified, the app needs permission to post notifications and schedule exact alarms. This is only used to show a notification when the timer ends.") },
            confirmButton = {
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                    showPermissionRationale = false
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                Button(onClick = { showPermissionRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    val sectionModifier = Modifier
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .clip(MaterialTheme.shapes.extraLarge)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)

    val settingsRowModifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .padding(horizontal = 16.dp)

    val settingsTextStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
        ) {
            // Appearance Section
            SettingsHeader(title = "Appearance")
            Column(
                modifier = sectionModifier
            ) {
                ThemeSetting(
                    selectedTheme = uiState.theme,
                    onThemeChanged = viewModel::onThemeChanged,
                )
                SettingsDivider()
                Row(
                    modifier = settingsRowModifier.clickable { onAccentColorClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ColorLens, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Color Scheme", style = settingsTextStyle)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color = FastTimesTheme.accentColor, shape = CircleShape)
                    )
                }
                SettingsDivider()
                FirstDayOfWeekSetting(
                    selectedFirstDayOfWeek = uiState.firstDayOfWeek,
                    onFirstDayOfWeekChanged = viewModel::onFirstDayOfWeekChanged
                )
                SettingsDivider()
                Row(
                    modifier = settingsRowModifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Show Floating Action Button", style = settingsTextStyle)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = uiState.showFab,
                        onCheckedChange = viewModel::onShowFabChanged
                    )
                }
                SettingsDivider()
                Row(
                    modifier = settingsRowModifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircleOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Use Expressive Progress Indicator", style = settingsTextStyle)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = uiState.useWavyIndicator,
                        onCheckedChange = viewModel::onUseWavyIndicatorChanged
                    )
                }
            }

            // Notifications Section
            SettingsHeader(title = "Notifications")
            Column(
                modifier = sectionModifier
            ) {
                Row(
                    modifier = settingsRowModifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Timelapse, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Show live progress notification", style = settingsTextStyle)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = uiState.showLiveProgress,
                        onCheckedChange = { show ->
                            if (show) {
                                handlePermission { viewModel.onShowLiveProgressChanged(true) }
                            } else {
                                viewModel.onShowLiveProgressChanged(false)
                            }
                        }
                    )
                }
                SettingsDivider()
                Row(
                    modifier = settingsRowModifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Goal reached notification", style = settingsTextStyle)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = uiState.showGoalReachedNotification,
                        onCheckedChange = { show ->
                            if (show) {
                                handlePermission { viewModel.onShowGoalReachedNotificationChanged(true) }
                            } else {
                                viewModel.onShowGoalReachedNotificationChanged(false)
                            }
                        }
                    )
                }
            }

            // Data Management Section
            SettingsHeader(title = "Data Management")
            Column(
                modifier = sectionModifier.fillMaxWidth()
            ) {
                FlowRow(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val buttonColors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )

                    Button(
                        onClick = { exportLauncher.launch("fasts.json") },
                        colors = buttonColors
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Export Data")
                    }

                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        colors = buttonColors
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Import Data")
                    }
                }
            }
            AboutCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeSetting(
    selectedTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val settingsTextStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = { expanded = true })
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = Icons.Default.Palette, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text("Theme", style = settingsTextStyle)
            Spacer(modifier = Modifier.weight(1f))
            Text(selectedTheme.name.lowercase().replaceFirstChar { it.uppercase() })
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            AppTheme.entries.forEach { theme ->
                DropdownMenuItem(
                    text = { Text(text = theme.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelLarge) },
                    onClick = {
                        onThemeChanged(theme)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FirstDayOfWeekSetting(
    selectedFirstDayOfWeek: String,
    onFirstDayOfWeekChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val days = listOf("Sunday", "Monday")
    val settingsTextStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = { expanded = true })
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text("First Day of Week", style = settingsTextStyle)
            Spacer(modifier = Modifier.weight(1f))
            Text(selectedFirstDayOfWeek)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            days.forEach { day ->
                DropdownMenuItem(
                    text = { Text(day, style = MaterialTheme.typography.labelLarge) },
                    onClick = {
                        onFirstDayOfWeekChanged(day)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.height(5.dp),
        color = MaterialTheme.colorScheme.surface,
        thickness = 2.dp
    )
}