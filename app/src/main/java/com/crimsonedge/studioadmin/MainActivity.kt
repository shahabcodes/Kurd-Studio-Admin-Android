package com.crimsonedge.studioadmin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crimsonedge.studioadmin.data.local.ThemeDataStore
import com.crimsonedge.studioadmin.data.local.TokenDataStore
import com.crimsonedge.studioadmin.data.security.SecurityManager
import com.crimsonedge.studioadmin.domain.security.SecurityCheckResult
import com.crimsonedge.studioadmin.presentation.navigation.AppNavigation
import com.crimsonedge.studioadmin.presentation.navigation.Screen
import com.crimsonedge.studioadmin.presentation.security.BiometricLockScreen
import com.crimsonedge.studioadmin.presentation.security.SecurityBlockScreen
import com.crimsonedge.studioadmin.ui.theme.AppTheme
import com.crimsonedge.studioadmin.ui.theme.StudioAdminTheme
import com.crimsonedge.studioadmin.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var tokenDataStore: TokenDataStore

    @Inject
    lateinit var themeDataStore: ThemeDataStore

    @Inject
    lateinit var securityManager: SecurityManager

    private var securityResult by mutableStateOf<SecurityCheckResult?>(null)
    private var biometricPassed by mutableStateOf(false)
    private var biometricRequired by mutableStateOf(true)

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Run security checks during splash (fast, ~10-50ms)
        securityResult = securityManager.performChecks()

        // Set up biometric authentication (read preference synchronously — fast local read)
        val biometricEnabled = runBlocking { themeDataStore.biometricEnabled.first() }
        setupBiometric(biometricEnabled)

        // Keep splash visible while auth state loads
        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        enableEdgeToEdge()
        setContent {
            val themeModeStr by themeDataStore.themeMode
                .collectAsStateWithLifecycle(initialValue = "SYSTEM")
            val appThemeStr by themeDataStore.appTheme
                .collectAsStateWithLifecycle(initialValue = "ROSE")

            val themeMode = ThemeMode.entries.find { it.name == themeModeStr } ?: ThemeMode.SYSTEM
            val appTheme = AppTheme.entries.find { it.name == appThemeStr } ?: AppTheme.ROSE

            StudioAdminTheme(themeMode = themeMode, appTheme = appTheme) {
                val result = securityResult
                when {
                    result != null && result.isCompromised -> {
                        isReady = true
                        SecurityBlockScreen(failureReasons = result.failureReasons)
                    }
                    biometricRequired && !biometricPassed -> {
                        isReady = true
                        BiometricLockScreen(onAuthenticate = ::showBiometricPrompt)
                    }
                    else -> {
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
                                    tokenDataStore = tokenDataStore,
                                    themeDataStore = themeDataStore
                                )
                            }
                            false -> {
                                isReady = true
                                AppNavigation(
                                    startDestination = Screen.Login.route,
                                    tokenDataStore = tokenDataStore,
                                    themeDataStore = themeDataStore
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupBiometric(enabled: Boolean) {
        if (!enabled) {
            biometricRequired = false
            biometricPassed = true
            return
        }

        val biometricManager = BiometricManager.from(this)
        val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Device has biometric/credential — require authentication
                biometricRequired = true

                val executor = ContextCompat.getMainExecutor(this)
                biometricPrompt = BiometricPrompt(this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult
                        ) {
                            biometricPassed = true
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            // User cancelled or error — stay on lock screen
                        }

                        override fun onAuthenticationFailed() {
                            // Biometric didn't match — prompt stays open for retry
                        }
                    }
                )

                promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Kurd Studio")
                    .setSubtitle("Authenticate to open the app")
                    .setAllowedAuthenticators(authenticators)
                    .build()

                // Auto-show the prompt on launch
                biometricPrompt.authenticate(promptInfo)
            }
            else -> {
                // No biometric hardware, none enrolled, or unavailable — skip
                biometricRequired = false
                biometricPassed = true
            }
        }
    }

    private fun showBiometricPrompt() {
        if (::biometricPrompt.isInitialized) {
            biometricPrompt.authenticate(promptInfo)
        }
    }
}
