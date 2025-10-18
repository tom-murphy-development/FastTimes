package com.fasttimes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fasttimes.data.Theme
import com.fasttimes.ui.FastTimesNavHost
import com.fasttimes.ui.settings.SettingsUiState
import com.fasttimes.ui.settings.SettingsViewModel
import com.fasttimes.ui.theme.FastTimesTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var uiState: SettingsUiState by mutableStateOf(SettingsUiState.Loading)

        // Keep the splash screen on-screen until the UI state is loaded.
        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                SettingsUiState.Loading -> true
                is SettingsUiState.Success -> false
            }
        }

        // Start collecting the theme settings
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        setContent {
            val useDarkTheme = when (val state = uiState) {
                SettingsUiState.Loading -> isSystemInDarkTheme() // Default while loading
                is SettingsUiState.Success -> when (state.selectedTheme) {
                    Theme.LIGHT -> false
                    Theme.DARK -> true
                    Theme.SYSTEM -> isSystemInDarkTheme()
                }
            }

            FastTimesTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FastTimesNavHost()
                }
            }
        }
    }
}
