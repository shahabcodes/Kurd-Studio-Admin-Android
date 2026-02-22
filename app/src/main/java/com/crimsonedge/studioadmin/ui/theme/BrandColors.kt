package com.crimsonedge.studioadmin.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class BrandColors(
    val gradientStart: Color,
    val gradientEnd: Color
) {
    val gradient: Brush
        get() = Brush.linearGradient(
            colors = listOf(gradientStart, gradientEnd),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )

    val gradientHorizontal: Brush
        get() = Brush.horizontalGradient(
            colors = listOf(gradientStart, gradientEnd)
        )
}

val LocalBrandColors = staticCompositionLocalOf {
    BrandColors(
        gradientStart = Color(0xFFDB2777),
        gradientEnd = Color(0xFFA855F7)
    )
}

val LocalIsDarkTheme = staticCompositionLocalOf { false }
