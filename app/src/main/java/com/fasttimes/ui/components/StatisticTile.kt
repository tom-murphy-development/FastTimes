package com.fasttimes.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * A composable that displays a single statistic in a styled tile.
 *
 * This component is designed to be used within a grid or row to show key metrics
 * to the user in a visually engaging way. It includes an icon, a label, and the
 * corresponding value.
 *
 * @param modifier The modifier to be applied to the tile.
 * @param icon The [ImageVector] to display at the top of the tile.
 * @param label The text describing the statistic.
 * @param value The string representation of the statistic's value.
 * @param description An optional description to display at the bottom of the tile.
 * @param containerColor The color of the card's container.
 * @param contentColor The color of the card's content.
 * @param border An optional [BorderStroke] to apply to the card.
 * @param onClick An optional lambda to be invoked when the tile is clicked. If null, the tile will not be clickable.
 * @param onLongClick An optional lambda to be invoked when the tile is long-clicked.
 */
@Composable
fun StatisticTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    description: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val cardModifier = modifier.pointerInput(Unit) {
        detectTapGestures(
            onLongPress = { onLongClick?.invoke() },
            onTap = { onClick?.invoke() }
        )
    }

    Card(
        modifier = cardModifier,
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Decorative icon
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}