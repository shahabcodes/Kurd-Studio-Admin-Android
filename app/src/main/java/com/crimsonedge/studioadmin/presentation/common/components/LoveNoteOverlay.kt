package com.crimsonedge.studioadmin.presentation.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crimsonedge.studioadmin.ui.theme.Pink400
import com.crimsonedge.studioadmin.ui.theme.Pink500
import com.crimsonedge.studioadmin.ui.theme.Purple400
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun LoveNoteOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(800)) + scaleIn(tween(600), initialScale = 0.9f),
        exit = fadeOut(tween(400)) + scaleOut(tween(400), targetScale = 0.9f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A0A1E),
                            Color(0xFF2D1233),
                            Color(0xFF1A0A1E)
                        )
                    )
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Floating hearts background
            FloatingHeartsCanvas()

            // Message content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 36.dp)
            ) {
                // Pulsing heart
                val infiniteTransition = rememberInfiniteTransition(label = "heart_pulse")
                val heartScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "heart_scale"
                )

                Canvas(
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer {
                            scaleX = heartScale
                            scaleY = heartScale
                        }
                ) {
                    drawHeart(
                        center = Offset(size.width / 2, size.height / 2),
                        size = size.width * 0.45f,
                        color = Pink500
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "For My Everything",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Pink400,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "I built this little world of art and words,\n" +
                            "but nothing I could ever create\n" +
                            "comes close to how beautiful you are.\n\n" +
                            "You are the muse behind every brushstroke,\n" +
                            "the poetry woven into every line,\n" +
                            "and the most exquisite masterpiece\n" +
                            "I get to admire every single day.\n\n" +
                            "This app is my love letter to you\n" +
                            "— wrapped in code, sealed with a heartbeat.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 26.sp,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 0.3.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Forever yours \u2764\uFE0F",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Purple400
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "tap anywhere to close",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun HeartBurstOverlay(
    visible: Boolean,
    onFinished: () -> Unit
) {
    if (!visible) return

    val hearts = remember {
        List(20) {
            HeartParticle(
                x = (0.1f + Math.random().toFloat() * 0.8f),
                startY = -0.1f - Math.random().toFloat() * 0.3f,
                speed = 0.3f + Math.random().toFloat() * 0.5f,
                size = 12f + Math.random().toFloat() * 20f,
                swayAmplitude = 20f + Math.random().toFloat() * 30f,
                swayFrequency = 1f + Math.random().toFloat() * 2f,
                alpha = 0.5f + Math.random().toFloat() * 0.5f,
                color = if (Math.random() > 0.5) Pink500 else Pink400
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(3000, easing = LinearEasing))
        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val currentProgress = progress.value
        hearts.forEach { heart ->
            val y = heart.startY + currentProgress * (1.2f + heart.speed)
            if (y in -0.1f..1.1f) {
                val sway = sin(y * heart.swayFrequency * PI.toFloat() * 2) * heart.swayAmplitude
                val screenX = heart.x * size.width + sway
                val screenY = y * size.height
                val fadeAlpha = heart.alpha * (1f - (currentProgress * 0.5f).coerceAtMost(1f))
                drawHeart(
                    center = Offset(screenX, screenY),
                    size = heart.size,
                    color = heart.color.copy(alpha = fadeAlpha.coerceIn(0f, 1f))
                )
            }
        }
    }
}

@Composable
private fun FloatingHeartsCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_hearts")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val bgHearts = listOf(
            Triple(0.15f, 0.2f, 8f),
            Triple(0.85f, 0.15f, 10f),
            Triple(0.25f, 0.75f, 6f),
            Triple(0.75f, 0.8f, 12f),
            Triple(0.5f, 0.45f, 7f),
            Triple(0.1f, 0.5f, 9f),
            Triple(0.9f, 0.55f, 5f),
        )

        bgHearts.forEachIndexed { index, (baseX, baseY, heartSize) ->
            val phase = index * 0.7f
            val floatY = sin((time * PI * 2 + phase).toFloat()) * 15f
            val floatX = sin((time * PI * 2 * 0.7f + phase).toFloat()) * 8f
            drawHeart(
                center = Offset(
                    baseX * size.width + floatX,
                    baseY * size.height + floatY
                ),
                size = heartSize,
                color = Pink500.copy(alpha = 0.08f)
            )
        }
    }
}

private fun DrawScope.drawHeart(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y + size * 0.3f)
        cubicTo(
            center.x - size, center.y - size * 0.5f,
            center.x - size * 0.5f, center.y - size,
            center.x, center.y - size * 0.4f
        )
        cubicTo(
            center.x + size * 0.5f, center.y - size,
            center.x + size, center.y - size * 0.5f,
            center.x, center.y + size * 0.3f
        )
        close()
    }
    drawPath(path, color)
}

private data class HeartParticle(
    val x: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val swayAmplitude: Float,
    val swayFrequency: Float,
    val alpha: Float,
    val color: Color
)
