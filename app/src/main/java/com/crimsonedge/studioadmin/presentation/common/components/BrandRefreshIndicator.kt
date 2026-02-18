package com.crimsonedge.studioadmin.presentation.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.crimsonedge.studioadmin.ui.theme.BrandGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    refreshingContent: @Composable () -> Unit = { ShimmerListContent() },
    content: @Composable () -> Unit
) {
    val state = rememberPullToRefreshState()

    // Shimmer overlay is a SIBLING of PullToRefreshBox (not a child)
    // so PullToRefreshBox internal state can never suppress its recomposition
    Box(modifier = modifier) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
            state = state,
            indicator = {
                BrandPullIndicator(
                    isRefreshing = isRefreshing,
                    state = state,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            content()
        }

        // Skeleton shimmer overlay — lives outside PullToRefreshBox
        AnimatedVisibility(
            visible = isRefreshing,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                refreshingContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandPullIndicator(
    isRefreshing: Boolean,
    state: PullToRefreshState,
    modifier: Modifier = Modifier
) {
    // Hide during refresh — shimmer overlay handles the visual feedback
    if (isRefreshing) return

    val fraction = state.distanceFraction.coerceIn(0f, 1f)
    if (fraction > 0f) {
        Box(
            modifier = modifier
                .graphicsLayer {
                    translationY = fraction * 48f
                    alpha = fraction
                    scaleX = fraction
                }
                .width(48.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BrandGradient)
        )
    }
}
