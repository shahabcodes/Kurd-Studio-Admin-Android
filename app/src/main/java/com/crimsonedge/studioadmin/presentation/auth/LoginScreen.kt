package com.crimsonedge.studioadmin.presentation.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.crimsonedge.studioadmin.presentation.common.components.HeartBurstOverlay
import com.crimsonedge.studioadmin.presentation.common.components.LoveNoteOverlay
import com.crimsonedge.studioadmin.ui.theme.BrandGradient
import com.crimsonedge.studioadmin.ui.theme.Pink400
import com.crimsonedge.studioadmin.ui.theme.Pink500
import com.crimsonedge.studioadmin.ui.theme.Purple400
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Easter egg state
    var logoTapCount by remember { mutableIntStateOf(0) }
    var showLoveNote by remember { mutableStateOf(false) }

    // Heart confetti on login success
    var showHeartBurst by remember { mutableStateOf(false) }
    var loginTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && !loginTriggered) {
            loginTriggered = true
            showHeartBurst = true
        }
    }

    // ── Staggered entrance animations ──
    val entranceItems = remember { List(4) { Animatable(0f) } }
    LaunchedEffect(Unit) {
        entranceItems.forEachIndexed { index, anim ->
            launch {
                delay(index * 140L + 200L)
                anim.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
            }
        }
    }

    // ── Continuous animations ──
    val inf = rememberInfiniteTransition(label = "login")

    val orbTime by inf.animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "orbs"
    )

    val ringRotation by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Restart),
        label = "ring"
    )

    val heartbeatScale by inf.animateFloat(
        1f, 1f,
        infiniteRepeatable(
            keyframes {
                durationMillis = 1200
                1f at 0; 1.08f at 100; 1f at 200; 1.05f at 300; 1f at 400; 1f at 1200
            },
            RepeatMode.Restart
        ),
        label = "heartbeat"
    )

    val shimmerX by inf.animateFloat(
        -0.5f, 1.5f,
        infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )

    val particleTime by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Restart),
        label = "particles"
    )

    // ── UI ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08080F))
    ) {
        // Layer 1: Animated gradient orbs
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Pink500.copy(alpha = 0.18f), Color.Transparent)
                ),
                center = Offset(
                    (0.25f + 0.2f * sin(orbTime * 0.7f)) * size.width,
                    (0.15f + 0.1f * cos(orbTime * 0.5f)) * size.height
                ),
                radius = size.width * 0.6f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Purple400.copy(alpha = 0.14f), Color.Transparent)
                ),
                center = Offset(
                    (0.8f + 0.12f * sin(orbTime * 0.5f + 2f)) * size.width,
                    (0.5f + 0.12f * cos(orbTime * 0.6f + 1f)) * size.height
                ),
                radius = size.width * 0.5f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Pink400.copy(alpha = 0.10f), Color.Transparent)
                ),
                center = Offset(
                    (0.5f + 0.2f * sin(orbTime * 0.3f + 4f)) * size.width,
                    (0.85f + 0.08f * cos(orbTime * 0.4f + 3f)) * size.height
                ),
                radius = size.width * 0.55f
            )
        }

        // Layer 2: Floating particles
        FloatingParticles(time = particleTime)

        // Layer 3: Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(60.dp))

            // ── Logo with orbiting ring ──
            val p0 = entranceItems[0].value
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = p0
                    scaleX = 0.5f + p0 * 0.5f
                    scaleY = 0.5f + p0 * 0.5f
                },
                contentAlignment = Alignment.Center
            ) {
                // Soft glow behind logo
                Box(
                    Modifier
                        .size(130.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(Pink500.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                )

                // Orbiting gradient arc — glow layer
                Canvas(
                    Modifier
                        .size(96.dp)
                        .graphicsLayer { rotationZ = ringRotation }
                ) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to Pink500.copy(alpha = 0.12f),
                            0.35f to Color.Transparent,
                            1f to Color.Transparent
                        ),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to Pink500,
                            0.25f to Purple400,
                            0.38f to Color.Transparent,
                            1f to Color.Transparent
                        ),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Logo circle with heartbeat
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                            scaleX = heartbeatScale
                            scaleY = heartbeatScale
                        }
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Pink500, Purple400)))
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
            }

            Spacer(Modifier.height(32.dp))

            // ── Brand name — gradient text ──
            val p1 = entranceItems[1].value
            Text(
                text = "Kurd Studio",
                modifier = Modifier.graphicsLayer {
                    alpha = p1
                    translationY = (1f - p1) * 30f
                },
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    brush = Brush.linearGradient(
                        listOf(Color.White, Pink400, Purple400)
                    )
                )
            )

            Spacer(Modifier.height(4.dp))

            // ── Subtitle ──
            val p2 = entranceItems[2].value
            Text(
                text = "ADMIN PANEL",
                modifier = Modifier.graphicsLayer {
                    alpha = p2
                    translationY = (1f - p2) * 20f
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Light
                ),
                color = Color.White.copy(alpha = 0.4f)
            )

            Spacer(Modifier.height(44.dp))

            // ── Glass panel ──
            val p3 = entranceItems[3].value
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = p3
                        translationY = (1f - p3) * 50f
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.14f),
                                Color.White.copy(alpha = 0.03f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Username
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = viewModel::onUsernameChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Username") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Person, contentDescription = "Username")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = glassFieldColors()
                    )

                    // Password
                    var passwordVisible by rememberSaveable { mutableStateOf(false) }

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = "Password")
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Rounded.VisibilityOff
                                    else Icons.Rounded.Visibility,
                                    contentDescription = if (passwordVisible)
                                        "Hide password"
                                    else "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
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
                        shape = RoundedCornerShape(16.dp),
                        colors = glassFieldColors()
                    )

                    // Error
                    if (uiState.error != null) {
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF6B6B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Sign In button with shimmer sweep
                    val btnShape = RoundedCornerShape(16.dp)
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isLoading,
                        shape = btnShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(btnShape)
                                .background(
                                    brush = BrandGradient,
                                    alpha = if (!uiState.isLoading) 1f else 0.4f
                                )
                                .drawWithContent {
                                    drawContent()
                                    if (!uiState.isLoading) {
                                        drawRect(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.White.copy(alpha = 0.18f),
                                                    Color.Transparent
                                                ),
                                                start = Offset(
                                                    shimmerX * size.width,
                                                    0f
                                                ),
                                                end = Offset(
                                                    shimmerX * size.width + size.width * 0.35f,
                                                    size.height
                                                )
                                            )
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Sign In",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(60.dp))
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

// ── Glass-style text field colors ──

@Composable
private fun glassFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White.copy(alpha = 0.8f),
    focusedContainerColor = Color.White.copy(alpha = 0.06f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
    focusedBorderColor = Pink500,
    unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
    cursorColor = Pink400,
    focusedLeadingIconColor = Pink400,
    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.4f),
    focusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.4f),
    focusedPlaceholderColor = Color.White.copy(alpha = 0.35f),
    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.35f),
    errorBorderColor = Color(0xFFFF6B6B),
    errorTextColor = Color.White
)

// ── Floating particles ──

@Composable
private fun FloatingParticles(time: Float) {
    val particles = remember {
        List(20) {
            FloatParticle(
                x = Random.nextFloat(),
                baseY = Random.nextFloat(),
                speed = 0.3f + Random.nextFloat() * 0.6f,
                radius = 0.5f + Random.nextFloat() * 1.2f,
                alpha = 0.06f + Random.nextFloat() * 0.16f
            )
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val y = ((p.baseY - time * p.speed) % 1f + 1f) % 1f
            drawCircle(
                color = Color.White.copy(alpha = p.alpha),
                radius = p.radius.dp.toPx(),
                center = Offset(p.x * size.width, y * size.height)
            )
        }
    }
}

private data class FloatParticle(
    val x: Float,
    val baseY: Float,
    val speed: Float,
    val radius: Float,
    val alpha: Float
)
