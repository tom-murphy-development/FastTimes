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
package com.fasttimes.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fasttimes.data.DefaultFastingProfile
import com.fasttimes.data.fast.Fast
import com.fasttimes.ui.theme.FastTimesTheme
import java.time.Duration
import java.time.format.DateTimeFormatter

@Composable
fun FastingSummaryModal(
    fast: Fast,
    onDismiss: () -> Unit,
    onSaveRating: (Int) -> Unit
) {
    var rating by remember { mutableIntStateOf(fast.rating ?: 0) }
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    val duration = Duration.between(
        fast.start,
        fast.end
    )

    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fast Complete!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${hours}h ${minutes}m",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (fast.profileName != DefaultFastingProfile.OPEN.displayName) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = fast.profileName,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (fast.goalMet()) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Goal Reached",
                            tint = FastTimesTheme.accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Text(
                        text = "Started:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(70.dp)
                    )
                    Text(
                        fast.start.format(timeFormatter),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "Ended:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(70.dp)
                    )
                    Text(
                        fast.end?.format(timeFormatter) ?: "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
                            tint = if (star <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
        dismissButton = null
    )
}
