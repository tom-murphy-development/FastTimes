package com.fasttimes.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fasttimes.data.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNotificationPermissionRationale by remember { mutableStateOf(false) }
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

    if (showNotificationPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showNotificationPermissionRationale = false },
            title = { Text("Permission Required") },
            text = { Text("To show notifications, the app needs permission to post notifications.") },
            confirmButton = {
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    showNotificationPermissionRationale = false
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                Button(onClick = { showNotificationPermissionRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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
        Column(modifier = Modifier.padding(paddingValues)) {
            // Appearance Section
            SettingsHeader(title = "Appearance")
            ThemeSetting(
                selectedTheme = uiState.theme,
                onThemeChanged = viewModel::onThemeChanged,
            )
            FirstDayOfWeekSetting(
                selectedFirstDayOfWeek = uiState.firstDayOfWeek,
                onFirstDayOfWeekChanged = viewModel::onFirstDayOfWeekChanged
            )

            HorizontalDivider()

            // Notifications Section
            SettingsHeader(title = "Notifications")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Show live fast progress", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.showLiveProgress,
                    onCheckedChange = { show ->
                        if (show) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    viewModel.onShowLiveProgressChanged(true)
                                } else {
                                    permissionAction = { viewModel.onShowLiveProgressChanged(true) }
                                    showNotificationPermissionRationale = true
                                }
                            } else {
                                viewModel.onShowLiveProgressChanged(true)
                            }
                        } else {
                            viewModel.onShowLiveProgressChanged(false)
                        }
                    }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Goal reached notification", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.showGoalReachedNotification,
                    onCheckedChange = { show ->
                        if (show) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    viewModel.onShowGoalReachedNotificationChanged(true)
                                } else {
                                    permissionAction = { viewModel.onShowGoalReachedNotificationChanged(true) }
                                    showNotificationPermissionRationale = true
                                }
                            } else {
                                viewModel.onShowGoalReachedNotificationChanged(true)
                            }
                        } else {
                            viewModel.onShowGoalReachedNotificationChanged(false)
                        }
                    }
                )
            }

            HorizontalDivider()

            // Data Management Section
            SettingsHeader(title = "Data Management")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { exportLauncher.launch("fasts.json") }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Export Data", style = MaterialTheme.typography.bodyLarge)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { importLauncher.launch(arrayOf("application/json")) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Import Data", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ThemeSetting(
    selectedTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { expanded = true })
            .padding(horizontal = 16.dp, vertical = 12.dp)

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Theme", style = MaterialTheme.typography.bodyLarge)
            Text(selectedTheme.name.lowercase().replaceFirstChar { it.uppercase() })
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            AppTheme.entries.forEach { theme ->
                DropdownMenuItem(
                    text = { Text(text = theme.name.lowercase().replaceFirstChar { it.uppercase() }) },
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { expanded = true })
            .padding(horizontal = 16.dp, vertical = 12.dp)

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("First Day of Week", style = MaterialTheme.typography.bodyLarge)
            Text(selectedFirstDayOfWeek)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            days.forEach { day ->
                DropdownMenuItem(
                    text = { Text(text = day) },
                    onClick = {
                        onFirstDayOfWeekChanged(day)
                        expanded = false
                    }
                )
            }
        }
    }
}
