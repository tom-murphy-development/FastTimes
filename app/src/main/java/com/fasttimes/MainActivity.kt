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

    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var dashboardUiState: DashboardUiState by mutableStateOf(DashboardUiState.Loading)

        // Keep the splash screen on-screen until the UI state is loaded.
        splashScreen.setKeepOnScreenCondition {
            dashboardUiState is DashboardUiState.Loading
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.uiState
                    .onEach { dashboardUiState = it }
                    .collect()
            }
        }

        setContent {
            FastTimesTheme {
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
