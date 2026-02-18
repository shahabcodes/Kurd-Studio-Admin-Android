package com.crimsonedge.studioadmin.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crimsonedge.studioadmin.presentation.common.components.GradientButton
import com.crimsonedge.studioadmin.presentation.common.components.HeartBurstOverlay
import com.crimsonedge.studioadmin.presentation.common.components.LoveNoteOverlay
import com.crimsonedge.studioadmin.ui.theme.Pink500
import com.crimsonedge.studioadmin.ui.theme.Purple400

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var isVisible by remember { mutableStateOf(false) }

    // Easter egg state
    var logoTapCount by remember { mutableIntStateOf(0) }
    var showLoveNote by remember { mutableStateOf(false) }

    // Heart confetti on login success
    var showHeartBurst by remember { mutableStateOf(false) }
    var loginTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && !loginTriggered) {
            loginTriggered = true
            showHeartBurst = true
        }
    }

    // Heartbeat animation for the logo
    val heartbeat = rememberInfiniteTransition(label = "heartbeat")
    val heartbeatScale by heartbeat.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                1f at 0
                1.08f at 100
                1f at 200
                1.05f at 300
                1f at 400
                1f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "heartbeat_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Pink500.copy(alpha = 0.15f),
                        Purple400.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background
                    ),
                    center = Offset(0.5f, 0.3f),
                    radius = 1200f
                )
            )
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(
                        animationSpec = tween(600),
                        initialOffsetY = { it / 6 }
                    )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Brand gradient circle with "K" - heartbeat animation + tap easter egg
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .graphicsLayer {
                                    scaleX = heartbeatScale
                                    scaleY = heartbeatScale
                                }
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Pink500, Purple400)
                                    )
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    logoTapCount++
                                    if (logoTapCount >= 5) {
                                        logoTapCount = 0
                                        showLoveNote = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "K",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title
                        Text(
                            text = "Kurd Studio",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Subtitle
                        Text(
                            text = "Admin Panel",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Username field
                        OutlinedTextField(
                            value = uiState.username,
                            onValueChange = viewModel::onUsernameChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Username") },
                            placeholder = { Text("Enter your username") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = "Username",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Pink500,
                                focusedLabelColor = Pink500,
                                cursorColor = Pink500
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password field
                        var passwordVisible by rememberSaveable { mutableStateOf(false) }

                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Password") },
                            placeholder = { Text("Enter your password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = "Password",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Rounded.VisibilityOff
                                        } else {
                                            Icons.Rounded.Visibility
                                        },
                                        contentDescription = if (passwordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        },
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.login()
                                }
                            ),
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Pink500,
                                focusedLabelColor = Pink500,
                                cursorColor = Pink500
                            )
                        )

                        // Error text
                        if (uiState.error != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.error!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Sign In button
                        GradientButton(
                            text = "Sign In",
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.login()
                            },
                            isLoading = uiState.isLoading,
                            enabled = !uiState.isLoading
                        )
                    }
                }
            }
        }

        // Heart burst confetti on login success
        HeartBurstOverlay(
            visible = showHeartBurst,
            onFinished = {
                showHeartBurst = false
                onLoginSuccess()
            }
        )

        // Love note easter egg overlay
        LoveNoteOverlay(
            visible = showLoveNote,
            onDismiss = { showLoveNote = false }
        )
    }
}
