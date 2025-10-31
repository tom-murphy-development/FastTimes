package com.fasttimes.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.fasttimes.data.profile.FastingProfile
import java.util.concurrent.TimeUnit

@Composable
fun AddEditProfileDialog(
    profile: FastingProfile?,
    onDismiss: () -> Unit,
    onSave: (name: String, duration: Long?) -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var hours by remember(profile) {
        mutableStateOf(profile?.duration?.let { TimeUnit.MILLISECONDS.toHours(it) }?.toString() ?: "")
    }
    var minutes by remember(profile) {
        mutableStateOf(
            profile?.duration?.let { (TimeUnit.MILLISECONDS.toMinutes(it) % 60) }?.toString() ?: ""
        )
    }
    val isEditing = profile != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Profile" else "Add Profile") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") }
                )
                TextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Hours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = minutes,
                    onValueChange = { minutes = it },
                    label = { Text("Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hoursLong = hours.toLongOrNull() ?: 0L
                    val minutesLong = minutes.toLongOrNull() ?: 0L
                    val totalMillis = TimeUnit.HOURS.toMillis(hoursLong) + TimeUnit.MINUTES.toMillis(minutesLong)
                    onSave(name, totalMillis.takeIf { it > 0 })
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
        text = { Text("Are you sure you want to delete the profile \"${profile.name}\"? This action cannot be undone.") },
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
