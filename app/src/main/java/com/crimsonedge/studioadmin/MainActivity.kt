package com.crimsonedge.studioadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crimsonedge.studioadmin.data.local.TokenDataStore
import com.crimsonedge.studioadmin.presentation.navigation.AppNavigation
import com.crimsonedge.studioadmin.presentation.navigation.Screen
import com.crimsonedge.studioadmin.ui.theme.StudioAdminTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenDataStore: TokenDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash visible while auth state loads
        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        enableEdgeToEdge()
        setContent {
            StudioAdminTheme {
                val isLoggedIn by tokenDataStore.isLoggedIn
                    .collectAsStateWithLifecycle(initialValue = null)

                when (isLoggedIn) {
                    null -> {
                        // Still loading auth state — splash screen is visible
                    }
                    true -> {
                        isReady = true
                        AppNavigation(
                            startDestination = Screen.Dashboard.route,
                            tokenDataStore = tokenDataStore
                        )
                    }
                    false -> {
                        isReady = true
                        AppNavigation(
                            startDestination = Screen.Login.route,
                            tokenDataStore = tokenDataStore
                        )
                    }
                }
            }
        }
    }
}
