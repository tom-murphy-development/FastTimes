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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fasttimes.data.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
        .clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surfaceContainer)

    val settingsRowModifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
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

            ThemeSelectionCard(
                selectedTheme = uiState.theme,
                onThemeChanged = viewModel::onThemeChanged,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ThemeColorCard(
                useSystemColors = uiState.useSystemColors,
                selectedSeedColor = uiState.seedColor,
                onUseSystemColorsChanged = viewModel::onUseSystemColorsChanged,
                onSeedColorChanged = viewModel::onSeedColorChanged,
                onCustomColorClick = onAccentColorClick,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(
                modifier = sectionModifier
            ) {
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
private fun ThemeSelectionCard(
    selectedTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    ExpressiveCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeModeOption(
                icon = Icons.Default.LightMode,
                label = "Light",
                isSelected = selectedTheme == AppTheme.LIGHT,
                onClick = { onThemeChanged(AppTheme.LIGHT) },
                modifier = Modifier.weight(1f)
            )

            ThemeModeOption(
                icon = Icons.Default.DarkMode,
                label = "Dark",
                isSelected = selectedTheme == AppTheme.DARK,
                onClick = { onThemeChanged(AppTheme.DARK) },
                modifier = Modifier.weight(1f)
            )

            ThemeModeOption(
                icon = Icons.Default.Palette,
                label = "System",
                isSelected = selectedTheme == AppTheme.SYSTEM,
                onClick = { onThemeChanged(AppTheme.SYSTEM) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeModeOption(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ScaleAnimation"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeColorCard(
    useSystemColors: Boolean,
    selectedSeedColor: Long?,
    onUseSystemColorsChanged: (Boolean) -> Unit,
    onSeedColorChanged: (Long) -> Unit,
    onCustomColorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        0xFF2196F3, // Blue
        0xFF4CAF50, // Green
        0xFFFF9800, // Orange
        0xFF9C27B0, // Purple
        0xFFF44336  // Red
    )

    ExpressiveCard(modifier = modifier) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
            Text(
                text = "Color Scheme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Dynamic Color Option
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    item {
                        ThemeColorOption(
                            color = null,
                            icon = Icons.Default.AutoAwesome,
                            isSelected = useSystemColors,
                            onClick = { onUseSystemColorsChanged(true) },
                            label = "Dynamic"
                        )
                    }
                }

                // Preset Colors
                items(presets) { colorLong ->
                    ThemeColorOption(
                        color = Color(colorLong),
                        isSelected = !useSystemColors && selectedSeedColor == colorLong,
                        onClick = {
                            onUseSystemColorsChanged(false)
                            onSeedColorChanged(colorLong)
                        },
                        label = "" // Labels handled by icons or omitted for brevity
                    )
                }

                // Custom Color Option
                item {
                    ThemeColorOption(
                        color = null,
                        icon = Icons.Default.Colorize,
                        isSelected = false,
                        onClick = onCustomColorClick,
                        label = "Custom"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeColorOption(
    color: Color?,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    label: String = ""
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ColorScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .clip(if (isSelected) MaterialShapes.Cookie9Sided.toShape() else CircleShape)
                .background(color ?: MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = if (isSelected) MaterialShapes.Cookie9Sided.toShape() else CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected && color != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FirstDayOfWeekSetting(
    selectedFirstDayOfWeek: String,
    onFirstDayOfWeekChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("Sunday", "Monday")
    val settingsTextStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text("First Day of Week", style = settingsTextStyle)
        Spacer(modifier = Modifier.weight(1f))

        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = { onFirstDayOfWeekChanged(label) },
                    selected = selectedFirstDayOfWeek == label,
                    label = { Text(label.take(3)) }
                )
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.height(1.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 1.dp
    )
}

@Composable
private fun ExpressiveCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(20.dp),
        content = { content() }
    )
}
