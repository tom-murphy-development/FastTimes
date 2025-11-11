package com.fasttimes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fasttimes.ui.FastTimesNavHost
import com.fasttimes.ui.dashboard.DashboardUiState
import com.fasttimes.ui.dashboard.DashboardViewModel
import com.fasttimes.ui.theme.FastTimesTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var uiState: MainUiState by mutableStateOf(mainViewModel.uiState.value)
        var dashboardUiState: DashboardUiState by mutableStateOf(dashboardViewModel.uiState.value)

        /*// Keep the splash screen on-screen until the UI state is loaded.
        splashScreen.setKeepOnScreenCondition {
            uiState.isLoading || dashboardUiState is DashboardUiState.Loading
        }*/

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.uiState
                    .onEach { dashboardUiState = it }
                    .collect()
            }
        }

        setContent {
            val seedColor = uiState.userData.seedColor?.let { Color(it) } ?: Color(0xFF3DDC84)
            val accentColor = uiState.userData.accentColor?.let { Color(it) } ?: Color(0xFF3DDC84)

            FastTimesTheme(
                theme = uiState.userData.theme,
                seedColor = seedColor,
                accentColor = accentColor,
                useExpressiveTheme = uiState.userData.useExpressiveTheme,
                useSystemColors = uiState.userData.useSystemColors
            ) {
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
