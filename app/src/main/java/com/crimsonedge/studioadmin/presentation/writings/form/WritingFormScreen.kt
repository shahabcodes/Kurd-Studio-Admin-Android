package com.crimsonedge.studioadmin.presentation.writings.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.crimsonedge.studioadmin.presentation.common.components.FormDropdown
import com.crimsonedge.studioadmin.presentation.common.components.FormTextField
import com.crimsonedge.studioadmin.presentation.common.components.GradientButton
import com.crimsonedge.studioadmin.presentation.common.components.LoadingShimmer
import com.crimsonedge.studioadmin.ui.theme.Pink500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingFormScreen(
    navController: NavController,
    viewModel: WritingFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate back on save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.popBackStack()
        }
    }

    // Show errors via snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Determine if the selected type is "novel-chapter"
    val selectedTypeName = uiState.types.firstOrNull { it.id == uiState.writingTypeId }?.typeName
    val isNovelChapter = selectedTypeName == "novel-chapter"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditing) "Edit Writing" else "New Writing",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingShimmer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Title
                FormTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    label = "Title",
                    isError = uiState.titleError != null,
                    errorText = uiState.titleError,
                    modifier = Modifier.fillMaxWidth()
                )

                // Slug with auto-generate button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormTextField(
                        value = uiState.slug,
                        onValueChange = viewModel::updateSlug,
                        label = "Slug",
                        isError = uiState.slugError != null,
                        errorText = uiState.slugError,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = viewModel::generateSlug,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Generate slug from title"
                        )
                    }
                }

                // Writing type dropdown
                if (uiState.types.isNotEmpty()) {
                    FormDropdown(
                        selectedValue = uiState.writingTypeId.toString(),
                        options = uiState.types.map { it.id.toString() to it.displayName },
                        onOptionSelected = { value ->
                            value.toIntOrNull()?.let { viewModel.updateWritingTypeId(it) }
                        },
                        label = "Writing Type",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Subtitle
                FormTextField(
                    value = uiState.subtitle,
                    onValueChange = viewModel::updateSubtitle,
                    label = "Subtitle",
                    modifier = Modifier.fillMaxWidth()
                )

                // Excerpt
                FormTextField(
                    value = uiState.excerpt,
                    onValueChange = viewModel::updateExcerpt,
                    label = "Excerpt",
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Full content
                FormTextField(
                    value = uiState.fullContent,
                    onValueChange = viewModel::updateFullContent,
                    label = "Full Content",
                    singleLine = false,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date published
                FormTextField(
                    value = uiState.datePublished,
                    onValueChange = viewModel::updateDatePublished,
                    label = "Date Published",
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Novel name - conditional on type
                AnimatedVisibility(visible = isNovelChapter) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FormTextField(
                            value = uiState.novelName,
                            onValueChange = viewModel::updateNovelName,
                            label = "Novel Name",
                            modifier = Modifier.fillMaxWidth()
                        )

                        FormTextField(
                            value = uiState.chapterNumber,
                            onValueChange = viewModel::updateChapterNumber,
                            label = "Chapter Number",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Display order
                FormTextField(
                    value = uiState.displayOrder,
                    onValueChange = viewModel::updateDisplayOrder,
                    label = "Display Order",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save button
                GradientButton(
                    text = "Save Writing",
                    onClick = viewModel::save,
                    isLoading = uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
