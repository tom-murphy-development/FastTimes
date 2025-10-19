package com.fasttimes.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fasttimes.data.FastingProfile

/**
 * A modal dialog that displays details about a [FastingProfile] and asks for confirmation
 * before starting the fast.
 *
 * This composable is stateless and follows Unidirectional Data Flow principles.
 *
 * @param profile The [FastingProfile] to display details for.
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
    // Note: This preview will only work if FastingProfile.MANUAL is a static object
    // with a valid displayName, which is inferred from the DashboardScreen code.
    ProfileDetailsModal(
        profile = FastingProfile.MANUAL,
        onDismiss = {},
        onConfirm = {}
    )
}
