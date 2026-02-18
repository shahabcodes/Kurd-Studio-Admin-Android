package com.crimsonedge.studioadmin.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.crimsonedge.studioadmin.ui.theme.Pink500
import com.crimsonedge.studioadmin.ui.theme.Purple400

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val navRoute: String = route
)

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(
            label = "Dashboard",
            icon = Icons.Rounded.Dashboard,
            route = Screen.Dashboard.route
        ),
        BottomNavItem(
            label = "Content",
            icon = Icons.Rounded.Palette,
            route = Screen.Content.route,
            navRoute = Screen.Content.createRoute()
        ),
        BottomNavItem(
            label = "Images",
            icon = Icons.Rounded.Image,
            route = Screen.ImageList.route
        ),
        BottomNavItem(
            label = "Config",
            icon = Icons.Rounded.Settings,
            route = Screen.SiteConfig.route
        ),
        BottomNavItem(
            label = "More",
            icon = Icons.Rounded.MoreHoriz,
            route = Screen.More.route
        )
    )

    val haptic = LocalHapticFeedback.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedIndex = items.indexOfFirst { currentRoute == it.route }.coerceAtLeast(0)

    // Animated indicator position with spring physics
    val indicatorPosition by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "nav_indicator"
    )

    val pillGradientStart = Pink500.copy(alpha = 0.14f)
    val pillGradientEnd = Purple400.copy(alpha = 0.14f)
    val topLineColor = Pink500.copy(alpha = 0.3f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(64.dp)
                .drawBehind {
                    val itemCount = items.size
                    val itemWidth = size.width / itemCount

                    // Sliding gradient pill
                    val pillWidthRatio = 0.58f
                    val pillWidth = itemWidth * pillWidthRatio
                    val pillHeight = 34.dp.toPx()
                    val pillX = indicatorPosition * itemWidth + (itemWidth - pillWidth) / 2
                    val pillY = 6.dp.toPx()

                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(pillGradientStart, pillGradientEnd),
                            startX = pillX,
                            endX = pillX + pillWidth
                        ),
                        topLeft = Offset(pillX, pillY),
                        size = Size(pillWidth, pillHeight),
                        cornerRadius = CornerRadius(pillHeight / 2, pillHeight / 2)
                    )

                    // Top accent line that tracks the indicator
                    val lineWidth = itemWidth * 0.35f
                    val lineX = indicatorPosition * itemWidth + (itemWidth - lineWidth) / 2
                    val lineHeight = 2.5.dp.toPx()
                    val lineRadius = lineHeight / 2

                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Pink500, Purple400),
                            startX = lineX,
                            endX = lineX + lineWidth
                        ),
                        topLeft = Offset(lineX, 0f),
                        size = Size(lineWidth, lineHeight),
                        cornerRadius = CornerRadius(lineRadius, lineRadius)
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
                    label = "icon_scale_$index"
                )

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Pink500 else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200),
                    label = "nav_color_$index"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (item.navRoute == Screen.Dashboard.route) {
                                    navController.popBackStack(
                                        route = Screen.Dashboard.route,
                                        inclusive = false
                                    )
                                } else {
                                    navController.navigate(item.navRoute) {
                                        popUpTo(Screen.Dashboard.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = contentColor,
                        modifier = Modifier.graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
