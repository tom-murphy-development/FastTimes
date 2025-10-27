package com.fasttimes.ui.editfast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fasttimes.data.FastingProfile
import com.fasttimes.data.fast.Fast
import com.fasttimes.ui.components.DateTimeDialog
import com.fasttimes.ui.components.RatingDialog
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun EditFastRoute(
    onDismiss: () -> Unit,
    fastId: Long? = null
) {
    val viewModel: EditFastViewModel = hiltViewModel(
        key = "edit-fast-$fastId",
        creationCallback = { factory: EditFastViewModel.Factory ->
            factory.create(fastId)
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    EditFastScreen(
        uiState = uiState,
        onStartTimeChanged = viewModel::updateStartTime,
        onEndTimeChanged = viewModel::updateEndTime,
        onRatingChanged = viewModel::updateRating,
        onSave = {
            viewModel.saveChanges()
            onDismiss()
        },
        onCancel = onDismiss,
        onErrorDismissed = viewModel::clearError
    )
}

private enum class PickerType {
    None, Start, End, Rating
}

@Composable
fun EditFastScreen(
    uiState: EditFastUiState,
    onStartTimeChanged: (Long) -> Unit,
    onEndTimeChanged: (Long) -> Unit,
    onRatingChanged: (Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(PickerType.None) }

    if (showPicker != PickerType.None && uiState.fast != null) {
        when (showPicker) {
            PickerType.Start -> DateTimeDialog(
                initialMillis = uiState.fast.startTime,
                onDismiss = { showPicker = PickerType.None },
                onConfirm = {
                    onStartTimeChanged(it)
                    showPicker = PickerType.None
                }
            )
            PickerType.End -> DateTimeDialog(
                initialMillis = uiState.fast.endTime!!,
                onDismiss = { showPicker = PickerType.None },
                onConfirm = {
                    onEndTimeChanged(it)
                    showPicker = PickerType.None
                }
            )
            PickerType.Rating -> RatingDialog(
                initialRating = uiState.fast.rating,
                onDismiss = { showPicker = PickerType.None },
                onConfirm = {
                    onRatingChanged(it)
                    showPicker = PickerType.None
                }
            )
            else -> {}
        }
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.fast != null) {
                val fast = uiState.fast
                Text("Edit Fast", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                EditableFastDetailRow(
                    label = "Start Time",
                    value = formatTimestamp(fast.startTime),
                    onClick = { showPicker = PickerType.Start }
                )

                if (fast.endTime != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableFastDetailRow(
                        label = "End Time",
                        value = formatTimestamp(fast.endTime),
                        onClick = { showPicker = PickerType.End }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    EditableFastDetailRow(
                        label = "Rating",
                        value = fast.rating?.toString() ?: "Not set",
                        onClick = { showPicker = PickerType.Rating }
                    )
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                    Button(onClick = onSave) {
                        Text("Save")
                    }
                }
            } else {
                Text("Fast not found.")
            }
        }
    }
}

@Composable
private fun EditableFastDetailRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        TextButton(onClick = onClick) {
            Text(value)
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
}

@Preview(showBackground = true)
@Composable
private fun EditFastScreenInProgressPreview() {
    MaterialTheme {
        EditFastScreen(
            uiState = EditFastUiState(
                isLoading = false,
                fast = Fast(
                    id = 1L,
                    startTime = System.currentTimeMillis() - 1000 * 60 * 60 * 4,
                    endTime = null,
                    targetDuration = 16 * 60 * 60 * 1000,
                    profile = FastingProfile.getById("16/8")!!
                )
            ),
            onStartTimeChanged = {},
            onEndTimeChanged = {},
            onRatingChanged = {},
            onSave = {},
            onCancel = {},
            onErrorDismissed = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun EditFastScreenFinishedPreview() {
    MaterialTheme {
        EditFastScreen(
            uiState = EditFastUiState(
                isLoading = false,
                fast = Fast(
                    id = 1L,
                    startTime = System.currentTimeMillis() - 1000 * 60 * 60 * 20,
                    endTime = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                    targetDuration = 16 * 60 * 60 * 1000,
                    profile = FastingProfile.getById("16/8")!!,
                    rating = 4
                )
            ),
            onStartTimeChanged = {},
            onEndTimeChanged = {},
            onRatingChanged = {},
            onSave = {},
            onCancel = {},
            onErrorDismissed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditFastScreenWithErrorPreview() {
    MaterialTheme {
        EditFastScreen(
            uiState = EditFastUiState(
                isLoading = false,
                error = "End time must be after start time.",
                fast = Fast(
                    id = 1L,
                    startTime = System.currentTimeMillis() - 1000 * 60 * 60 * 20,
                    endTime = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                    targetDuration = 16 * 60 * 60 * 1000,
                    profile = FastingProfile.getById("16/8")!!,
                    rating = 4
                )
            ),
            onStartTimeChanged = {},
            onEndTimeChanged = {},
            onRatingChanged = {},
            onSave = {},
            onCancel = {},
            onErrorDismissed = {}
        )
    }
}
