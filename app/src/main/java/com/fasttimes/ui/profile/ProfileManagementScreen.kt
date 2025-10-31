package com.fasttimes.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fasttimes.data.profile.FastingProfile
import java.util.concurrent.TimeUnit

@Composable
fun ProfileManagementRoute(
    onBackClick: () -> Unit,
    viewModel: ProfileManagementViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    ProfileManagementScreen(
        profiles = profiles,
        onAddProfile = { name, duration -> viewModel.addProfile(name, duration) },
        onUpdateProfile = { viewModel.updateProfile(it) },
        onDeleteProfile = { viewModel.deleteProfile(it) },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileManagementScreen(
    profiles: List<FastingProfile>,
    onAddProfile: (name: String, duration: Long?) -> Unit,
    onUpdateProfile: (FastingProfile) -> Unit,
    onDeleteProfile: (FastingProfile) -> Unit,
    onBackClick: () -> Unit
) {
    var showAddEditDialog by remember { mutableStateOf<FastingProfile?>(null) }
    var showDeleteDialog by remember { mutableStateOf<FastingProfile?>(null) }

    showAddEditDialog?.let { profile ->
        AddEditProfileDialog(
            profile = profile,
            onDismiss = { showAddEditDialog = null },
            onSave = { name, duration ->
                if (profile.id != 0L) {
                    onUpdateProfile(profile.copy(name = name, duration = duration))
                } else {
                    onAddProfile(name, duration)
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
                },
                actions = {
                    IconButton(onClick = { showAddEditDialog = FastingProfile(id = 0L, name = "", duration = null, description = "") }) {
                        Icon(Icons.Filled.Add, "Add Profile")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(profiles) { profile ->
                ProfileListItem(
                    profile = profile,
                    isDeleteEnabled = profiles.size > 1,
                    onUpdate = { showAddEditDialog = it },
                    onDelete = { showDeleteDialog = it },
                )
            }
        }
    }
}

@Composable
fun ProfileListItem(
    profile: FastingProfile,
    isDeleteEnabled: Boolean,
    onUpdate: (FastingProfile) -> Unit,
    onDelete: (FastingProfile) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUpdate(profile) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = profile.name)
            val duration = profile.duration
            if (duration != null && duration > 0) {
                val hours = TimeUnit.MILLISECONDS.toHours(duration)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60

                val durationText = when {
                    hours > 0 -> "$hours hr, $minutes min"
                    minutes > 0 -> "$minutes min, $seconds sec"
                    else -> "$seconds sec"
                }
                Text(text = durationText)
            } else {
                Text(text = "Manual")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onUpdate(profile) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
            }
            IconButton(onClick = { onDelete(profile) }, enabled = isDeleteEnabled) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Profile")
            }
        }
    }
}
