package com.crimsonedge.studioadmin.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.crimsonedge.studioadmin.ui.theme.BrandGradient

@Composable
fun BrandLogo(modifier: Modifier = Modifier) {
    var tapCount by remember { mutableIntStateOf(0) }
    var showLoveNote by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(BrandGradient)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                tapCount++
                if (tapCount >= 5) {
                    tapCount = 0
                    showLoveNote = true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "K",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = Color.White
        )
    }

    if (showLoveNote) {
        Dialog(
            onDismissRequest = { showLoveNote = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            LoveNoteOverlay(
                visible = true,
                onDismiss = { showLoveNote = false }
            )
        }
    }
}
