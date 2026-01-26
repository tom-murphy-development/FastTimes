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
package com.tmdev.fasttimes.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.tmdev.fasttimes.data.profile.FastingProfile

/**
 * A modal dialog that displays details about a [FastingProfiles] and asks for confirmation
 * before starting the fast.
 *
 * This composable is stateless and follows Unidirectional Data Flow principles.
 *
 * @param profile The [FastingProfiles] to display details for.
 * @param onDismiss Lambda to be invoked when the user dismisses the dialog (e.g., by clicking outside or pressing back).
 * @param onConfirm Lambda to be invoked when the user confirms the action to start the fast.
 */
@Composable
fun ProfileDetailsModal(
    profile: FastingProfile,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Info, contentDescription = "Profile Details") },
        title = {
            Text(text = profile.displayName)
        },
        text = {
            Text("Do you want to start this ${profile.displayName} fast?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Start Fast")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileDetailsModalPreview() {
    ProfileDetailsModal(
        profile = FastingProfile(
            displayName = "Manual",
            duration = null,
            description = "A manually controlled fast."
        ),
        onDismiss = {},
        onConfirm = {}
    )
}
