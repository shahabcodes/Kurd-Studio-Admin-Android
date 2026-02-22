package com.crimsonedge.studioadmin.presentation.siteconfig.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.crimsonedge.studioadmin.BuildConfig
import com.crimsonedge.studioadmin.presentation.common.components.ErrorState
import com.crimsonedge.studioadmin.presentation.common.components.FormSectionCard
import com.crimsonedge.studioadmin.presentation.common.components.FormTextField
import com.crimsonedge.studioadmin.presentation.common.components.GradientButton
import com.crimsonedge.studioadmin.presentation.common.components.ImagePickerDialog
import com.crimsonedge.studioadmin.presentation.common.components.LoadingShimmer

@Composable
fun ProfileEditorScreen(
    viewModel: ProfileEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showImagePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Profile saved successfully")
            viewModel.clearSaveSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            if (!uiState.isLoading) {
                snackbarHostState.showSnackbar(error)
            }
        }
    }

    // Image Picker Dialog
    ImagePickerDialog(
        isOpen = showImagePicker,
        onImageSelected = { imageId ->
            viewModel.updateAvatarImageId(imageId)
        },
        onDismiss = { showImagePicker = false }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                LoadingShimmer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                )
            }

            uiState.error != null && uiState.name.isEmpty() -> {
                ErrorState(
                    message = uiState.error ?: "Something went wrong",
                    onRetry = { viewModel.loadProfile() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar Section
                    FormSectionCard(
                        title = "Avatar",
                        icon = Icons.Rounded.CameraAlt,
                        subtitle = "Profile picture"
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { showImagePicker = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.avatarImageId != null) {
                                    AsyncImage(
                                        model = "${BuildConfig.API_BASE_URL}images/${uiState.avatarImageId}/thumbnail",
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CameraAlt,
                                            contentDescription = "Select avatar",
                                            modifier = Modifier.size(36.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Tap to change avatar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Personal Section
                    FormSectionCard(
                        title = "Personal",
                        icon = Icons.Rounded.Person,
                        subtitle = "Name, tagline and bio"
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FormTextField(
                                value = uiState.name,
                                onValueChange = viewModel::updateName,
                                label = "Name",
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                isError = uiState.name.isBlank() && uiState.error != null
                            )

                            FormTextField(
                                value = uiState.tagline,
                                onValueChange = viewModel::updateTagline,
                                label = "Tagline"
                            )

                            FormTextField(
                                value = uiState.bio,
                                onValueChange = viewModel::updateBio,
                                label = "Bio",
                                singleLine = false,
                                maxLines = 5
                            )
                        }
                    }

                    // Social Links Section
                    FormSectionCard(
                        title = "Social Links",
                        icon = Icons.Rounded.Share,
                        subtitle = "Email and social media URLs"
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FormTextField(
                                value = uiState.email,
                                onValueChange = viewModel::updateEmail,
                                label = "Email",
                                keyboardType = KeyboardType.Email,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            )

                            FormTextField(
                                value = uiState.instagramUrl,
                                onValueChange = viewModel::updateInstagramUrl,
                                label = "Instagram URL",
                                keyboardType = KeyboardType.Uri,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.AlternateEmail,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )

                            FormTextField(
                                value = uiState.twitterUrl,
                                onValueChange = viewModel::updateTwitterUrl,
                                label = "Twitter URL",
                                keyboardType = KeyboardType.Uri,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.AlternateEmail,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            )
                        }
                    }

                    // Statistics Section
                    FormSectionCard(
                        title = "Statistics",
                        icon = Icons.Rounded.BarChart,
                        subtitle = "Artworks, poems and experience"
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FormTextField(
                                    value = uiState.artworksCount,
                                    onValueChange = viewModel::updateArtworksCount,
                                    label = "Artworks",
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Brush,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )

                                FormTextField(
                                    value = uiState.poemsCount,
                                    onValueChange = viewModel::updatePoemsCount,
                                    label = "Poems",
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Numbers,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                )
                            }

                            FormTextField(
                                value = uiState.yearsExperience,
                                onValueChange = viewModel::updateYearsExperience,
                                label = "Years of Experience",
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save Button
                    GradientButton(
                        text = "Save Profile",
                        onClick = { viewModel.save() },
                        isLoading = uiState.isSaving,
                        enabled = uiState.name.isNotBlank()
                    )

                    Spacer(modifier = Modifier.height(32.dp))
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
