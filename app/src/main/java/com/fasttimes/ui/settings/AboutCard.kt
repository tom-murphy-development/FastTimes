package com.fasttimes.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fasttimes.BuildConfig
import com.fasttimes.R

// Taken from https://github.com/shub39/Grit/blob/master/app/src/main/java/com/shub39/grit/core/presentation/settings/ui/component/AboutApp.kt
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutCard(
    // isPlus: Boolean,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer
        ),
        shape = shapes.extraLarge
    ) {
        val buttonColors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.onPrimaryContainer,
            contentColor = colorScheme.primaryContainer
        )

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Text(
                    //if (!isPlus)
                    stringResource(R.string.app_name),
                    // else stringResource(R.string.app_name_plus),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = MaterialTheme.typography.titleLarge.fontFamily
                )
                Text(text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            }

            Spacer(modifier = Modifier.weight(1f))

            Row {
                IconButton(
                    onClick = { uriHandler.openUri("https://github.com/tom-murphy-development/FastTimes") },
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        painterResource(R.drawable.github),
                        contentDescription = "GitHub",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        FlowRow(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { uriHandler.openUri("https://www.buymeacoffee.com/tommurphydp") },
                colors = buttonColors
            ) {
                Icon(
                    painter = painterResource(R.drawable.bmac),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "BuyMeACoffee")
            }
            // Button(
            //    onClick = { /* TODO: Add action */ },
            //    colors = buttonColors
            // ) {
            //    Text(text = "BuyPlus")
            // }
        }
}}
