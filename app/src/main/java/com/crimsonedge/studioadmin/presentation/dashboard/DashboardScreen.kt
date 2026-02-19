package com.crimsonedge.studioadmin.presentation.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.crimsonedge.studioadmin.presentation.common.components.BrandPullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.crimsonedge.studioadmin.domain.model.DashboardStats
import com.crimsonedge.studioadmin.domain.util.Resource
import com.crimsonedge.studioadmin.presentation.common.components.ErrorState
import com.crimsonedge.studioadmin.presentation.common.components.LoadingShimmer
import com.crimsonedge.studioadmin.presentation.navigation.Screen
import com.crimsonedge.studioadmin.presentation.common.components.BrandLogo
import com.crimsonedge.studioadmin.presentation.common.modifiers.scaleOnPress
import com.crimsonedge.studioadmin.ui.theme.Pink400
import com.crimsonedge.studioadmin.ui.theme.Purple400
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val displayName by viewModel.displayName.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var cachedStats by remember { mutableStateOf<DashboardStats?>(null) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(dashboardState) {
        if (dashboardState is Resource.Success) {
            cachedStats = (dashboardState as Resource.Success).data
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BrandLogo()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { scaffoldPadding ->
    BrandPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadStats()
            scope.launch {
                viewModel.dashboardState.first { it !is Resource.Loading }
                isRefreshing = false
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Welcome Section
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500)) +
                        slideInVertically(
                            animationSpec = tween(500),
                            initialOffsetY = { -it / 4 }
                        )
            ) {
                Column {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = displayName ?: "Sanya",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Content based on state
            when {
                dashboardState is Resource.Loading && cachedStats == null -> {
                    LoadingShimmer(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                dashboardState is Resource.Error && cachedStats == null -> {
                    ErrorState(
                        message = (dashboardState as Resource.Error).message,
                        onRetry = { viewModel.loadStats() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp)
                    )
                }

                else -> {
                    val stats = (dashboardState as? Resource.Success)?.data ?: cachedStats
                    if (stats != null) {
                        DashboardContent(
                            stats = stats,
                            navController = navController,
                            isVisible = isVisible
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun DashboardContent(
    stats: DashboardStats,
    navController: NavController,
    isVisible: Boolean
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                slideInVertically(
                    animationSpec = tween(600, delayMillis = 200),
                    initialOffsetY = { it / 6 }
                )
    ) {
        Column {
            // Stats Grid - 2x2
            val statItems = listOf(
                StatItem(
                    label = "Artworks",
                    count = stats.artworkCount,
                    icon = Icons.Rounded.Palette,
                    tintColor = Pink400,
                    backgroundColor = Pink400.copy(alpha = 0.12f),
                    route = Screen.Content.createRoute(0)
                ),
                StatItem(
                    label = "Writings",
                    count = stats.writingCount,
                    icon = Icons.Rounded.EditNote,
                    tintColor = Purple400,
                    backgroundColor = Purple400.copy(alpha = 0.12f),
                    route = Screen.Content.createRoute(1)
                ),
                StatItem(
                    label = "Images",
                    count = stats.imageCount,
                    icon = Icons.Rounded.Image,
                    tintColor = Color(0xFF3B82F6),
                    backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.12f),
                    route = Screen.ImageList.route
                ),
                StatItem(
                    label = "Unread Contacts",
                    count = stats.unreadContactCount,
                    icon = Icons.Rounded.Mail,
                    tintColor = Color(0xFFF97316),
                    backgroundColor = Color(0xFFF97316).copy(alpha = 0.12f),
                    route = Screen.ContactList.route
                )
            )

            // Using a non-scrollable grid by laying out 2 rows manually
            // (since we are inside a scrollable Column, nested LazyVerticalGrid would conflict)
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // First row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StatCard(
                        item = statItems[0],
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            navController.navigate(statItems[0].route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        item = statItems[1],
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            navController.navigate(statItems[1].route)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Second row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StatCard(
                        item = statItems[2],
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            navController.navigate(statItems[2].route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        item = statItems[3],
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            navController.navigate(statItems[3].route)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Actions Section
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AssistChip(
                    onClick = {
                        navController.navigate(Screen.ArtworkForm.createRoute(null))
                    },
                    label = { Text("New Artwork") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        leadingIconContentColor = Pink400,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                AssistChip(
                    onClick = {
                        navController.navigate(Screen.WritingForm.createRoute(null))
                    },
                    label = { Text("New Writing") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        leadingIconContentColor = Purple400,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                AssistChip(
                    onClick = {
                        navController.navigate(Screen.ImageList.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    label = { Text("Upload") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Upload,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        leadingIconContentColor = Color(0xFF3B82F6),
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatCard(
    item: StatItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .height(148.dp)
            .scaleOnPress(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon in colored circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(item.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = item.tintColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                AnimatedCounter(
                    targetValue = item.count,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun AnimatedCounter(
    targetValue: Int,
    style: androidx.compose.ui.text.TextStyle,
    color: Color
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue.toFloat(),
            tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Text(
        text = animatedValue.value.roundToInt().toString(),
        style = style,
        color = color
    )
}

private data class StatItem(
    val label: String,
    val count: Int,
    val icon: ImageVector,
    val tintColor: Color,
    val backgroundColor: Color,
    val route: String
)
