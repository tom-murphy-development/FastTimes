package com.fasttimes.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fasttimes.data.profile.FastingProfile
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Composable
fun AddEditProfileDialog(
    profile: FastingProfile?,
    onDismiss: () -> Unit,
    onSave: (name: String, duration: Long?, description: String, isFavorite: Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile?.displayName ?: "") }
    var description by remember(profile) { mutableStateOf(profile?.description ?: "") }
    var isFavorite by remember(profile) { mutableStateOf(profile?.isFavorite ?: false) }
    var duration by remember(profile) {
        mutableStateOf(
            profile?.duration?.milliseconds?.toComponents { hours, minutes, _, _ ->
                String.format("%02d%02d", hours, minutes)
            } ?: ""
        )
    }
    val isEditing = profile?.id != 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Profile" else "Add Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter { it.isDigit() }.take(4) },
                    label = { Text("Duration") },
                    placeholder = { Text("HH:MM") },
                    visualTransformation = TimeVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isFavorite = !isFavorite })
                {
                    Checkbox(
                        checked = isFavorite,
                        onCheckedChange = { isFavorite = it }
                    )
                    Text(text = "Set as Favourite")
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isEditing) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Profile",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val paddedDuration = duration.padStart(4, '0')
                        val hours = paddedDuration.substring(0, 2).toLongOrNull() ?: 0L
                        val minutes = paddedDuration.substring(2, 4).toLongOrNull() ?: 0L
                        val totalMillis = (hours.hours + minutes.minutes).inWholeMilliseconds
                        onSave(name, totalMillis.takeIf { it > 0 }, description, isFavorite)
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    profile: FastingProfile,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Profile") },
        text = { Text("Are you sure you want to delete the profile \"${profile.displayName}\"? This action cannot be undone.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
