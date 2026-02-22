package com.crimsonedge.studioadmin.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

@Deprecated("Use LocalBrandColors.current.gradient instead", ReplaceWith("LocalBrandColors.current.gradient"))
val BrandGradient = Brush.linearGradient(
    colors = listOf(Pink500, Purple400),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

@Deprecated("Use LocalBrandColors.current.gradientHorizontal instead", ReplaceWith("LocalBrandColors.current.gradientHorizontal"))
val BrandGradientHorizontal = Brush.horizontalGradient(
    colors = listOf(Pink500, Purple400)
)
