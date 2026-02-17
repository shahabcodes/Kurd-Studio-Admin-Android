package com.crimsonedge.studioadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudioAdminTheme {
                val isLoggedIn by tokenDataStore.isLoggedIn
                    .collectAsStateWithLifecycle(initialValue = null)

                when (isLoggedIn) {
                    null -> {
                        // Still loading auth state - show nothing or a splash
                    }
                    true -> {
                        AppNavigation(
                            startDestination = Screen.Dashboard.route,
                            tokenDataStore = tokenDataStore
                        )
                    }
                    false -> {
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
