package com.crimsonedge.studioadmin.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

val BrandGradient = Brush.linearGradient(
    colors = listOf(Pink500, Purple400),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

val BrandGradientHorizontal = Brush.horizontalGradient(
    colors = listOf(Pink500, Purple400)
)
