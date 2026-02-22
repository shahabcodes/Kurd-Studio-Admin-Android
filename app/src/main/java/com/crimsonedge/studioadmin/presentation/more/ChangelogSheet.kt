package com.crimsonedge.studioadmin.presentation.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crimsonedge.studioadmin.ui.theme.LocalBrandColors
import com.crimsonedge.studioadmin.ui.theme.LocalIsDarkTheme

// ── Data models ──────────────────────────────────────────────

enum class ChangeType(val label: String, val icon: ImageVector) {
    NEW("New", Icons.Rounded.AutoAwesome),
    IMPROVED("Improved", Icons.AutoMirrored.Rounded.TrendingUp),
    FIXED("Fixed", Icons.Rounded.BugReport)
}

data class ChangeItem(
    val type: ChangeType,
    val description: String
)

data class ReleaseNote(
    val version: String,
    val date: String,
    val changes: List<ChangeItem>
)

// ── Release history ──────────────────────────────────────────

val releaseNotes = listOf(
    ReleaseNote(
        version = "1.1",
        date = "February 22, 2026",
        changes = listOf(
            ChangeItem(ChangeType.NEW, "Biometric authentication on app launch"),
            ChangeItem(ChangeType.NEW, "Toggle to enable/disable biometric lock in settings"),
            ChangeItem(ChangeType.NEW, "Changelog with release notes in settings"),
            ChangeItem(ChangeType.IMPROVED, "Security section added to settings screen")
        )
    ),
    ReleaseNote(
        version = "1.0",
        date = "February 20, 2026",
        changes = listOf(
            ChangeItem(ChangeType.NEW, "Dashboard with site analytics overview"),
            ChangeItem(ChangeType.NEW, "Content, writings, and artworks management"),
            ChangeItem(ChangeType.NEW, "Image gallery with upload support"),
            ChangeItem(ChangeType.NEW, "Site configuration — hero, profile, sections"),
            ChangeItem(ChangeType.NEW, "Navigation and social links editor"),
            ChangeItem(ChangeType.NEW, "Contact messages viewer"),
            ChangeItem(ChangeType.NEW, "6 accent color themes with dark mode support"),
            ChangeItem(ChangeType.NEW, "Root detection and security checks"),
            ChangeItem(ChangeType.NEW, "Animated login screen with glass-morphism UI")
        )
    )
)

// ── Composable ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val brandColors = LocalBrandColors.current
    val isDark = LocalIsDarkTheme.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.NewReleases,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = brandColors.gradientStart
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "What's New",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Scrollable release list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                releaseNotes.forEachIndexed { index, release ->
                    ReleaseSection(
                        release = release,
                        isLatest = index == 0,
                        brandStart = brandColors.gradientStart,
                        brandEnd = brandColors.gradientEnd,
                        isDark = isDark
                    )

                    if (index < releaseNotes.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReleaseSection(
    release: ReleaseNote,
    isLatest: Boolean,
    brandStart: Color,
    brandEnd: Color,
    isDark: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Version row
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Version badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isLatest) brandStart.copy(alpha = if (isDark) 0.2f else 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "v${release.version}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = if (isLatest) brandStart else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isLatest) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(brandStart)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LATEST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = release.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // Changes grouped by type
        val grouped = release.changes.groupBy { it.type }
        ChangeType.entries.forEach { type ->
            val items = grouped[type] ?: return@forEach
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { item ->
                    ChangeRow(item = item, isDark = isDark)
                }
            }
        }
    }
}

@Composable
private fun ChangeRow(item: ChangeItem, isDark: Boolean) {
    val (bgColor, fgColor) = when (item.type) {
        ChangeType.NEW -> Pair(
            Color(0xFF059669).copy(alpha = if (isDark) 0.18f else 0.1f),
            Color(0xFF059669)
        )
        ChangeType.IMPROVED -> Pair(
            Color(0xFF2563EB).copy(alpha = if (isDark) 0.18f else 0.1f),
            Color(0xFF2563EB)
        )
        ChangeType.FIXED -> Pair(
            Color(0xFFEA580C).copy(alpha = if (isDark) 0.18f else 0.1f),
            Color(0xFFEA580C)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = item.type.icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = fgColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.type.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = fgColor
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )
    }
}
