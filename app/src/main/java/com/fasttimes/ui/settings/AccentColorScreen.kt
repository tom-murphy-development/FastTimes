package com.fasttimes.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.fasttimes.ui.theme.FastTimesTheme
import kotlinx.coroutines.launch

// 1. Define the static, non-composable part of the list at the top level.
private val staticAccentColors = listOf(
    Color(0xFF3DDC84),
    Color(0xFFFFBE0B),
    Color(0xFFFB5607),
    Color(0xFFFF006E),
    Color(0xFF8338EC),
    Color(0xFF3A86FF)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccentColorScreen(
    onNavigateUp: () -> Unit
) {    val themeViewModel = FastTimesTheme.themeViewModel
    val themeState by themeViewModel.themeState.collectAsState()
    val scope = rememberCoroutineScope()

    val tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer

    val accentColors = remember(tertiaryContainerColor) {
        staticAccentColors + tertiaryContainerColor
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Accent Color") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Choose your accent color",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(accentColors, key = { it.toArgb() }) { color ->
                    val selectedColor = Color(themeState.accentColor)
                    val isSelected = selectedColor == color
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                scope.launch { themeViewModel.setAccentColor(color.toArgb().toLong()) }
                            }
                            .border(
                                width = if (isSelected) 4.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
