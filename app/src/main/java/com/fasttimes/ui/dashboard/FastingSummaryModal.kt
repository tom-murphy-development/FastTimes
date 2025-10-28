package com.fasttimes.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fasttimes.data.fast.Fast
import java.time.Duration
import java.time.format.DateTimeFormatter

@Composable
fun FastingSummaryModal(
    fast: Fast,
    onDismiss: () -> Unit,
    onSaveRating: (Int) -> Unit
) {
    var rating by remember { mutableStateOf(fast.rating ?: 0) }
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    val duration = Duration.between(
        fast.start,
        fast.end
    )

    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fast Complete!") },
        text = {
            Column {
                Text("Duration: ${hours}h ${minutes}m")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Started: ${fast.start.format(timeFormatter)}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ended: ${fast.end?.format(timeFormatter)}")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Rate your fast:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (star <= rating) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { rating = star }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (rating > 0) {
                    onSaveRating(rating)
                }
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}
