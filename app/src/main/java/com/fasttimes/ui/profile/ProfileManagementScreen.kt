package com.fasttimes.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fasttimes.data.profile.FastingProfile
import com.fasttimes.ui.components.StatisticTile
import java.util.concurrent.TimeUnit

@Composable
fun ProfileManagementRoute(
    onBackClick: () -> Unit,
    viewModel: ProfileManagementViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    ProfileManagementScreen(
        profiles = profiles,
        onAddProfile = { name, duration, description -> viewModel.addProfile(name, duration, description) },
        onUpdateProfile = { viewModel.updateProfile(it) },
        onDeleteProfile = { viewModel.deleteProfile(it) },
        onSetFavorite = { viewModel.setFavoriteProfile(it) },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileManagementScreen(
    profiles: List<FastingProfile>,
    onAddProfile: (name: String, duration: Long?, description: String) -> Unit,
    onUpdateProfile: (FastingProfile) -> Unit,
    onDeleteProfile: (FastingProfile) -> Unit,
    onSetFavorite: (FastingProfile) -> Unit,
    onBackClick: () -> Unit
) {
    var showAddEditDialog by remember { mutableStateOf<FastingProfile?>(null) }
    var showDeleteDialog by remember { mutableStateOf<FastingProfile?>(null) }

    showAddEditDialog?.let { profile ->
        AddEditProfileDialog(
            profile = profile,
            onDismiss = { showAddEditDialog = null },
            onSave = { name, duration, description ->
                if (profile.id != 0L) {
                    onUpdateProfile(profile.copy(name = name, duration = duration, description = description))
                } else {
                    onAddProfile(name, duration, description)
                }
                showAddEditDialog = null
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
            FloatingActionButton(onClick = { showAddEditDialog = FastingProfile(id = 0L, name = "", duration = null, description = "") }) {
                Icon(Icons.Filled.Add, "Add Profile")
            }
        }
    ) { padding ->
        val favoriteProfile = profiles.firstOrNull { it.isFavorite }
        val otherProfiles = profiles.filter { !it.isFavorite }

        Column(modifier = Modifier.padding(padding)) {
            if (favoriteProfile != null) {
                Text(
                    text = "Favourite Fast",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                )
                StatisticTile(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    icon = Icons.Filled.Favorite,
                    label = favoriteProfile.name,
                    value = formatDuration(favoriteProfile.duration),
                    description = favoriteProfile.description,
                    onClick = { showAddEditDialog = favoriteProfile }
                )
            }

            if (otherProfiles.isNotEmpty()) {
                Text(
                    text = "Other Profiles",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(otherProfiles) { profile ->
                        StatisticTile(
                            modifier = Modifier.aspectRatio(1f),
                            icon = Icons.Filled.Star, // Placeholder
                            label = profile.name,
                            value = formatDuration(profile.duration),
                            description = profile.description,
                            onClick = { showAddEditDialog = profile },
                            onLongClick = { onSetFavorite(profile) }
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(duration: Long?): String {
    if (duration != null && duration > 0) {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60

        return when {
            hours > 0 -> "$hours hr, $minutes min"
            minutes > 0 -> "$minutes min, $seconds sec"
            else -> "$seconds sec"
        }
    } else {
        return "Manual"
    }
}
