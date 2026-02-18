package com.crimsonedge.studioadmin.presentation.siteconfig.sections

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crimsonedge.studioadmin.domain.model.Section
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
fun SectionsListScreen(
    viewModel: SectionsListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.sections) {
        if (uiState.sections !is Resource.Loading) {
            isRefreshing = false
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Section updated successfully")
            viewModel.clearSaveSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    // Edit Bottom Sheet
    val editState = uiState.editState
    if (uiState.editingSectionId != null && editState != null) {
        SectionEditBottomSheet(
            sectionKey = (uiState.sections as? Resource.Success)?.data
                ?.firstOrNull { it.id == uiState.editingSectionId }?.sectionKey ?: "",
            editState = editState,
            isSaving = uiState.isSaving,
            onTagChange = viewModel::updateTag,
            onTitleChange = viewModel::updateTitle,
            onSubtitleChange = viewModel::updateSubtitle,
            onDisplayOrderChange = viewModel::updateDisplayOrder,
            onIsActiveChange = viewModel::updateIsActive,
            onSave = { viewModel.save() },
            onDismiss = { viewModel.cancelEdit() }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val sectionsState = uiState.sections) {
            is Resource.Loading -> {
                LoadingShimmer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                )
            }

            is Resource.Error -> {
                ErrorState(
                    message = sectionsState.message,
                    onRetry = { viewModel.loadSections() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            }

            is Resource.Success -> {
                val sections = sectionsState.data

                if (sections.isEmpty()) {
                    EmptyState(
                        message = "No sections configured yet.",
                        icon = Icons.Rounded.ViewModule,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    )
                } else {
                    BrandPullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            viewModel.loadSections()
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
                                items = sections,
                                key = { it.id }
                            ) { section ->
                                SectionCard(
                                    section = section,
                                    onEditClick = { viewModel.startEdit(section) }
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
private fun SectionCard(
    section: Section,
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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Order indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Pink500.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = section.displayOrder.toString(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Pink500
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Section key and info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.sectionKey,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!section.title.isNullOrBlank()) {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Active indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (section.isActive) {
                                Pink500.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (section.isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (section.isActive) Pink500 else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Edit button
                IconButton(
                    onClick = onEditClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Pink500
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit section"
                    )
                }
            }

            // Summary info
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!section.tag.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Purple400.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = section.tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = Purple400
                        )
                    }
                }

                if (!section.subtitle.isNullOrBlank()) {
                    Text(
                        text = section.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionEditBottomSheet(
    sectionKey: String,
    editState: SectionEditState,
    isSaving: Boolean,
    onTagChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onDisplayOrderChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    FormBottomSheet(
        title = "Edit Section",
        onDismiss = onDismiss,
        onSave = onSave,
        saveText = "Save Section",
        isSaving = isSaving
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section key label
            Text(
                text = sectionKey,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FormTextField(
                value = editState.tag,
                onValueChange = onTagChange,
                label = "Tag"
            )

            FormTextField(
                value = editState.title,
                onValueChange = onTitleChange,
                label = "Title"
            )

            FormTextField(
                value = editState.subtitle,
                onValueChange = onSubtitleChange,
                label = "Subtitle",
                singleLine = false,
                maxLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FormTextField(
                    value = editState.displayOrder,
                    onValueChange = onDisplayOrderChange,
                    label = "Display Order",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )

                // Active switch
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = editState.isActive,
                        onCheckedChange = onIsActiveChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = Pink500,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}
