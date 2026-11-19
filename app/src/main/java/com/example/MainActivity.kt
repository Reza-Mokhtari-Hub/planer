package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.configureAppCheck
import com.example.ui.navigation.PlannerRootApp
import com.example.ui.theme.WeeklyPlannerTheme
import com.example.ui.viewmodel.PlannerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PlannerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Firebase App Check BEFORE enableEdgeToEdge, ViewModel access, or UI setup
        configureAppCheck(this, intent)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val windowSizeClass = calculateWindowSizeClass(this)

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                WeeklyPlannerTheme(selectedTheme = uiState.settings.theme) {
                    PlannerRootApp(
                        uiState = uiState,
                        viewModel = viewModel,
                        windowSizeClass = windowSizeClass
                    )
                }
            }
        }
    }
}
