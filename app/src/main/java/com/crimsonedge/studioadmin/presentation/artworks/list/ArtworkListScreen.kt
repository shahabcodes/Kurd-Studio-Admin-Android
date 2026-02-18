package com.crimsonedge.studioadmin.presentation.artworks.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.StarOutline
import com.crimsonedge.studioadmin.presentation.common.components.ConfirmDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.crimsonedge.studioadmin.presentation.common.components.BrandPullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.crimsonedge.studioadmin.BuildConfig
import com.crimsonedge.studioadmin.domain.model.Artwork
import com.crimsonedge.studioadmin.domain.util.Resource
import com.crimsonedge.studioadmin.presentation.common.components.BrandLogo
import com.crimsonedge.studioadmin.presentation.common.components.EmptyState
import com.crimsonedge.studioadmin.presentation.common.components.ErrorState
import com.crimsonedge.studioadmin.presentation.common.components.GradientSnackbarHost
import com.crimsonedge.studioadmin.presentation.common.components.ShimmerListContent
import com.crimsonedge.studioadmin.presentation.common.modifiers.scaleOnPress
import com.crimsonedge.studioadmin.presentation.navigation.Screen
import com.crimsonedge.studioadmin.ui.theme.BrandGradient
import com.crimsonedge.studioadmin.ui.theme.Pink500
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkListScreen(
    navController: NavController,
    viewModel: ArtworkListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }
    var cachedArtworks by remember { mutableStateOf<List<Artwork>>(emptyList()) }

    LaunchedEffect(uiState.artworks) {
        if (uiState.artworks is Resource.Success) {
            cachedArtworks = (uiState.artworks as Resource.Success).data
        }
        if (uiState.artworks !is Resource.Loading) {
            isRefreshing = false
        }
    }

    // Reload data when returning from form screen
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasResumed by remember { mutableStateOf(false) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (hasResumed) {
                    viewModel.loadArtworks()
                }
                hasResumed = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Extended FAB expanded state: expanded when at the top
    val isFabExpanded by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 && !lazyListState.isScrollInProgress
        }
    }

    // Search state
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Show delete errors via snackbar
    LaunchedEffect(uiState.deleteError) {
        uiState.deleteError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearDeleteError()
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate(Screen.ArtworkForm.createRoute(null))
                },
                containerColor = Pink500,
                contentColor = Color.White,
                expanded = isFabExpanded,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Artwork"
                    )
                },
                text = { Text("New Artwork") }
            )
        },
        snackbarHost = { GradientSnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search artworks...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Pink500,
                    cursorColor = Pink500
                )
            )

            // Filter chips row
            if (uiState.types.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedType == null,
                            onClick = { viewModel.setTypeFilter(null) },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    items(uiState.types) { type ->
                        FilterChip(
                            selected = uiState.selectedType == type.typeName,
                            onClick = { viewModel.setTypeFilter(type.typeName) },
                            label = { Text(type.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Content area
            when {
                uiState.artworks is Resource.Loading && cachedArtworks.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ShimmerListContent(modifier = Modifier.fillMaxSize())
                    }
                }
                uiState.artworks is Resource.Error && cachedArtworks.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorState(
                            message = (uiState.artworks as Resource.Error).message,
                            onRetry = { viewModel.loadArtworks() }
                        )
                    }
                }
                else -> {
                    val artworkList = (uiState.artworks as? Resource.Success)?.data ?: cachedArtworks
                    val filteredArtworks by remember(artworkList, searchQuery) {
                        derivedStateOf {
                            if (searchQuery.isBlank()) {
                                artworkList
                            } else {
                                artworkList.filter { artwork ->
                                    artwork.title.contains(searchQuery, ignoreCase = true)
                                }
                            }
                        }
                    }

                    BrandPullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            viewModel.loadArtworks()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (filteredArtworks.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyState(
                                    message = if (searchQuery.isNotBlank()) {
                                        "No artworks matching \"$searchQuery\""
                                    } else {
                                        "No artworks yet\nTap + to create your first artwork"
                                    }
                                )
                            }
                        } else {
                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 4.dp,
                                    bottom = 88.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(
                                    items = filteredArtworks,
                                    key = { _, artwork -> artwork.id }
                                ) { index, artwork ->
                                    val animProgress = remember { Animatable(0f) }

                                    LaunchedEffect(artwork.id) {
                                        delay((index * 60L).coerceAtMost(400L))
                                        animProgress.animateTo(
                                            1f,
                                            spring(
                                                dampingRatio = 0.65f,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .animateItem()
                                            .graphicsLayer {
                                                val progress = animProgress.value
                                                alpha = progress.coerceIn(0f, 1f)
                                                translationY = (1f - progress) * 60f
                                                scaleX = 0.85f + (progress * 0.15f)
                                                scaleY = 0.85f + (progress * 0.15f)
                                            }
                                    ) {
                                        SwipeToDeleteArtworkItem(
                                            artwork = artwork,
                                            onEdit = {
                                                navController.navigate(
                                                    Screen.ArtworkForm.createRoute(artwork.id)
                                                )
                                            },
                                            onDelete = { viewModel.deleteArtwork(artwork.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteArtworkItem(
    artwork: Artwork,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isRemoved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                showDeleteDialog = true
                false // Don't dismiss yet, wait for confirmation
            } else {
                false
            }
        }
    )

    // Confirm delete dialog
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete Artwork",
            message = "Are you sure you want to delete \"${artwork.title}\"? This action cannot be undone.",
            onConfirm = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                showDeleteDialog = false
                isRemoved = true
                onDelete()
            },
            onDismiss = {
                showDeleteDialog = false
                scope.launch { dismissState.snapTo(SwipeToDismissBoxValue.Settled) }
            },
            confirmText = "Delete",
            isDestructive = true
        )
    }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color by animateColorAsState(
                    targetValue = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                        else -> Color.Transparent
                    },
                    label = "swipe_color"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
            },
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true
        ) {
            ArtworkCard(
                artwork = artwork,
                onClick = onEdit
            )
        }
    }
}

@Composable
private fun ArtworkCard(
    artwork: Artwork,
    onClick: () -> Unit
) {
    val thumbnailUrl = "${BuildConfig.API_BASE_URL}images/${artwork.imageId}/thumbnail"

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scaleOnPress(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Card(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = artwork.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title, type badge, description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = artwork.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = artwork.typeDisplayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (!artwork.description.isNullOrBlank()) {
                    Text(
                        text = artwork.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Featured star
            if (artwork.isFeatured) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Featured",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFFFBBF24)
                )
            }
        }
    }
}
