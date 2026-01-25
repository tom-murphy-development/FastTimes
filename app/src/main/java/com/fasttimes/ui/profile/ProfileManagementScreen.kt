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
package com.fasttimes.ui.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fasttimes.data.profile.FastingProfile
import com.fasttimes.ui.components.StatisticTile
import com.fasttimes.ui.theme.FastTimesTheme
import com.fasttimes.ui.theme.contentColorFor
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ProfileManagementRoute(
    onBackClick: () -> Unit,
    viewModel: ProfileManagementViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    ProfileManagementScreen(
        profiles = profiles,
        onAddProfile = { name, duration, description, isFavorite -> viewModel.addProfile(name, duration, description, isFavorite) },
        onUpdateProfile = { viewModel.updateProfile(it) },
        onDeleteProfile = { viewModel.deleteProfile(it) },
        onSetFavorite = { viewModel.setFavoriteProfile(it) },
        onBackClick = onBackClick
    )
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun ProfileManagementScreen(
    profiles: List<FastingProfile>,
    onAddProfile: (name: String, duration: Long?, description: String, isFavorite: Boolean) -> Unit,
    onUpdateProfile: (FastingProfile) -> Unit,
    onDeleteProfile: (FastingProfile) -> Unit,
    onSetFavorite: (FastingProfile) -> Unit,
    onBackClick: () -> Unit
) {
    var showAddEditDialog by remember { mutableStateOf<FastingProfile?>(null) }
    var showDeleteDialog by remember { mutableStateOf<FastingProfile?>(null) }
    var selectedProfile by remember { mutableStateOf<FastingProfile?>(null) }
    val context = LocalContext.current

    if (selectedProfile != null) {
        BackHandler {
            selectedProfile = null
        }
    }

    showAddEditDialog?.let { profile ->
        AddEditProfileDialog(
            profile = profile,
            onDismiss = { showAddEditDialog = null },
            onSave = { name, duration, description, isFavorite ->
                if (profile.id != 0L) {
                    onUpdateProfile(profile.copy(displayName = name, duration = duration, description = description, isFavorite = isFavorite))
                } else {
                    onAddProfile(name, duration, description, isFavorite)
                }
                showAddEditDialog = null
            },
            onDeleteClick = {
                showAddEditDialog = null
                showDeleteDialog = profile
            }
        )
    }

    showDeleteDialog?.let {
        DeleteConfirmationDialog(
            profile = it,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                onDeleteProfile(it)
                showDeleteDialog = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Profiles") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedProfile == null) {
                FloatingActionButton(
                    onClick = { showAddEditDialog = FastingProfile(id = 0L, displayName = "", duration = null, description = "") },
                    containerColor = FastTimesTheme.accentColor,
                    contentColor = contentColorFor(backgroundColor = FastTimesTheme.accentColor),                    ) {
                    Icon(Icons.Filled.Add, "Add Profile")
                }
            }
        },
        bottomBar = {
            if (selectedProfile != null) {
                BottomAppBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            val buttons = listOf("Edit", "Favourite", "Delete")
                            buttons.forEachIndexed { index, label ->
                                ToggleButton(
                                    checked = false,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            when (label) {
                                                "Edit" -> showAddEditDialog = selectedProfile
                                                "Favourite" -> {
                                                    onSetFavorite(selectedProfile!!)
                                                    Toast.makeText(context, "'${selectedProfile?.displayName}' set as favorite.", Toast.LENGTH_SHORT).show()
                                                }
                                                "Delete" -> showDeleteDialog = selectedProfile
                                            }
                                            selectedProfile = null
                                        }
                                    },
                                    shapes = when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        buttons.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                                    modifier = Modifier.semantics { role = Role.Button }
                                ) {
                                    val icon = when (label) {
                                        "Edit" -> Icons.Default.Edit
                                        "Favourite" -> Icons.Default.Favorite
                                        else -> Icons.Default.Delete
                                    }
                                    Icon(
                                        icon,
                                        contentDescription = "$label Profile",
                                    )
                                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                                    Text(label)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        val favoriteProfile = profiles.firstOrNull { it.isFavorite }
        val otherProfiles = profiles.filter { !it.isFavorite }

        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "Favourite Fast",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
            )

            if (favoriteProfile != null) {
                val isSelected = selectedProfile == favoriteProfile
                StatisticTile(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    icon = Icons.Filled.Favorite,
                    label = favoriteProfile.displayName,
                    value = formatDuration(favoriteProfile.duration),
                    description = favoriteProfile.description,
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    onClick = {
                        selectedProfile = if (isSelected) null else favoriteProfile
                    },
                    onLongClick = {
                        selectedProfile = favoriteProfile
                    }
                )
            } else {
                Text(
                    text = "Press and hold a profile to add it as your favourite",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            if (otherProfiles.isNotEmpty()) {
                Text(
                    text = "Other Profiles",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(180.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = otherProfiles, key = { it.id }) { profile ->
                        val isSelected = selectedProfile == profile
                        StatisticTile(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Filled.Star,
                            label = profile.displayName,
                            value = formatDuration(profile.duration),
                            description = profile.description,
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            onClick = {
                                selectedProfile = if (isSelected) null else profile
                            },
                            onLongClick = {
                                selectedProfile = profile
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(duration: Long?): String {
    if (duration == null || duration <= 0) {
        return "Open"
    }
    val d = duration.milliseconds
    return d.toComponents { hours, minutes, seconds, _ ->
        if (hours > 0) {
            val hourText = if (hours == 1L) "Hour" else "Hours"
            if (minutes > 0) {
                val minuteText = if (minutes == 1) "Minute" else "Minutes"
                "$hours $hourText, $minutes $minuteText"
            } else {
                "$hours $hourText"
            }
        } else if (minutes > 0) {
            val minuteText = if (minutes == 1) "Minute" else "Minutes"
            if (seconds > 0) {
                val secondText = if (seconds == 1) "Second" else "Seconds"
                "$minutes $minuteText, $seconds $secondText"
            } else {
                "$minutes $minuteText"
            }
        } else {
            val secondText = if (seconds == 1) "Second" else "Seconds"
            "$seconds $secondText"
        }
    }
}
