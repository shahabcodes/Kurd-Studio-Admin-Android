package com.crimsonedge.studioadmin.presentation.siteconfig.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.crimsonedge.studioadmin.presentation.common.components.BrandPullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crimsonedge.studioadmin.domain.model.SiteSetting
import com.crimsonedge.studioadmin.domain.util.Resource
import com.crimsonedge.studioadmin.presentation.common.components.EmptyState
import com.crimsonedge.studioadmin.presentation.common.components.ErrorState
import com.crimsonedge.studioadmin.presentation.common.components.FormBottomSheet
import com.crimsonedge.studioadmin.presentation.common.components.FormTextField
import com.crimsonedge.studioadmin.presentation.common.components.LoadingShimmer
import com.crimsonedge.studioadmin.ui.theme.Pink500
import com.crimsonedge.studioadmin.ui.theme.Purple400

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsListScreen(
    viewModel: SettingsListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.settings) {
        if (uiState.settings !is Resource.Loading) {
            isRefreshing = false
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Setting updated successfully")
            viewModel.clearSaveSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    // Edit Bottom Sheet
    if (uiState.editingKey != null) {
        val editingSetting = (uiState.settings as? Resource.Success)?.data
            ?.firstOrNull { it.settingKey == uiState.editingKey }

        SettingEditBottomSheet(
            settingKey = uiState.editingKey ?: "",
            settingType = editingSetting?.settingType ?: "",
            editValue = uiState.editingValue,
            isSaving = uiState.isSaving,
            onValueChange = viewModel::updateEditValue,
            onSave = { viewModel.saveEdit() },
            onDismiss = { viewModel.cancelEdit() }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val settingsState = uiState.settings) {
            is Resource.Loading -> {
                LoadingShimmer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                )
            }

            is Resource.Error -> {
                ErrorState(
                    message = settingsState.message,
                    onRetry = { viewModel.loadSettings() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            }

            is Resource.Success -> {
                val settings = settingsState.data

                if (settings.isEmpty()) {
                    EmptyState(
                        message = "No settings configured yet.",
                        icon = Icons.Rounded.Settings,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    )
                } else {
                    BrandPullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            viewModel.loadSettings()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 16.dp,
                                bottom = 32.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = settings,
                                key = { it.id }
                            ) { setting ->
                                SettingCard(
                                    setting = setting,
                                    onEditClick = { viewModel.startEdit(setting) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SettingCard(
    setting: SiteSetting,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Setting key
                    Text(
                        text = setting.settingKey,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Type badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Purple400.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = setting.settingType,
                            style = MaterialTheme.typography.labelSmall,
                            color = Purple400
                        )
                    }
                }

                IconButton(
                    onClick = onEditClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Pink500
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit setting"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Current value display
            Text(
                text = setting.settingValue ?: "(empty)",
                style = MaterialTheme.typography.bodyMedium,
                color = if (setting.settingValue != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingEditBottomSheet(
    settingKey: String,
    settingType: String,
    editValue: String,
    isSaving: Boolean,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    FormBottomSheet(
        title = "Edit Setting",
        onDismiss = onDismiss,
        onSave = onSave,
        isSaving = isSaving
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Setting key label
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = settingKey,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Purple400.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = settingType,
                        style = MaterialTheme.typography.labelSmall,
                        color = Purple400
                    )
                }
            }

            FormTextField(
                value = editValue,
                onValueChange = onValueChange,
                label = "Value",
                singleLine = false,
                maxLines = 5
            )
        }
    }
}
