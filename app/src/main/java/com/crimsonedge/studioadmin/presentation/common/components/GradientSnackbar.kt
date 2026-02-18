package com.crimsonedge.studioadmin.presentation.common.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.crimsonedge.studioadmin.ui.theme.Pink500

@Composable
fun GradientSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            BrandSnackbar(data)
        }
    )
}

@Composable
private fun BrandSnackbar(data: SnackbarData) {
    Snackbar(
        snackbarData = data,
        containerColor = Pink500,
        contentColor = Color.White,
        actionColor = Color.White.copy(alpha = 0.9f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}
