package com.crimsonedge.studioadmin.presentation.common.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crimsonedge.studioadmin.ui.theme.BrandGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            BrandRefreshIndicator(
                isRefreshing = isRefreshing,
                state = state,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandRefreshIndicator(
    isRefreshing: Boolean,
    state: PullToRefreshState,
    modifier: Modifier = Modifier
) {
    // Continuous spin while refreshing
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing)
        ),
        label = "refresh_rotation"
    )

    // Pull-down progress rotation (follows finger drag)
    val pullRotation = state.distanceFraction * 360f

    // Smooth entry animation
    val entryScale = remember { Animatable(0f) }
    LaunchedEffect(state.distanceFraction > 0f || isRefreshing) {
        if (state.distanceFraction > 0f || isRefreshing) {
            entryScale.animateTo(1f, tween(200))
        } else {
            entryScale.animateTo(0f, tween(150))
        }
    }

    val rotation = if (isRefreshing) spinRotation else pullRotation
    val scale = if (isRefreshing) 1f else state.distanceFraction.coerceIn(0f, 1f)
    val alpha = if (isRefreshing) 1f else state.distanceFraction.coerceIn(0f, 1f)

    // Vertical offset — follows the pull distance
    val translationY = if (isRefreshing) {
        64f
    } else {
        (state.distanceFraction * 100f).coerceAtMost(100f)
    }

    if (alpha > 0f) {
        Box(
            modifier = modifier
                .graphicsLayer {
                    this.translationY = translationY
                    this.scaleX = scale * entryScale.value
                    this.scaleY = scale * entryScale.value
                    this.alpha = alpha
                    rotationZ = rotation
                }
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = Color(0x40DB2777),
                    spotColor = Color(0x40DB2777)
                )
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BrandGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "K",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = Color.White
            )
        }
    }
}
