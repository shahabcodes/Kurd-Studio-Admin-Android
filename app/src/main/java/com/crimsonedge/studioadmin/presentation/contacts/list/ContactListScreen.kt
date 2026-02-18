package com.crimsonedge.studioadmin.presentation.contacts.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.crimsonedge.studioadmin.presentation.common.components.BrandPullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.crimsonedge.studioadmin.domain.model.Contact
import com.crimsonedge.studioadmin.domain.util.Resource
import com.crimsonedge.studioadmin.presentation.common.components.ConfirmDialog
import com.crimsonedge.studioadmin.presentation.common.components.EmptyState
import com.crimsonedge.studioadmin.presentation.common.components.ErrorState
import com.crimsonedge.studioadmin.presentation.common.components.GradientSnackbarHost
import com.crimsonedge.studioadmin.presentation.common.components.LoadingShimmer
import com.crimsonedge.studioadmin.presentation.common.modifiers.scaleOnPress
import com.crimsonedge.studioadmin.presentation.navigation.Screen
import com.crimsonedge.studioadmin.ui.theme.Pink500
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    navController: NavController,
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.contacts) {
        if (uiState.contacts !is Resource.Loading) {
            isRefreshing = false
        }
    }

    LaunchedEffect(uiState.deleteError) {
        uiState.deleteError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearDeleteError()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Contacts",
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
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
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
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search contacts...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                shape = MaterialTheme.shapes.extraLarge,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = !uiState.unreadOnly,
                        onClick = { viewModel.setUnreadFilter(false) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.unreadOnly,
                        onClick = { viewModel.setUnreadFilter(true) },
                        label = { Text("Unread Only") },
                        leadingIcon = if (uiState.unreadOnly) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Mail,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (val contacts = uiState.contacts) {
                is Resource.Loading -> {
                    LoadingShimmer(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    )
                }

                is Resource.Error -> {
                    ErrorState(
                        message = contacts.message,
                        onRetry = { viewModel.loadContacts() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is Resource.Success -> {
                    val filteredContacts = remember(contacts.data, searchQuery) {
                        if (searchQuery.isBlank()) contacts.data
                        else contacts.data.filter { contact ->
                            contact.name.contains(searchQuery, ignoreCase = true) ||
                                    contact.subject.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredContacts.isEmpty()) {
                        EmptyState(
                            message = if (searchQuery.isNotBlank()) "No contacts matching \"$searchQuery\""
                            else if (uiState.unreadOnly) "No unread contacts"
                            else "No contacts yet",
                            icon = Icons.Rounded.Inbox,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        BrandPullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = {
                                isRefreshing = true
                                viewModel.loadContacts()
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
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
                                    items = filteredContacts,
                                    key = { _, contact -> contact.id }
                                ) { index, contact ->
                                    val animProgress = remember { Animatable(0f) }

                                    LaunchedEffect(contact.id) {
                                        kotlinx.coroutines.delay((index * 50L).coerceAtMost(500L))
                                        animProgress.animateTo(1f, tween(300))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .animateItem()
                                            .graphicsLayer {
                                                alpha = animProgress.value
                                                translationY = (1f - animProgress.value) * 40f
                                            }
                                    ) {
                                        SwipeToDeleteContactItem(
                                            contact = contact,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                navController.navigate(
                                                    Screen.ContactDetail.createRoute(contact.id)
                                                )
                                            },
                                            onDelete = { viewModel.deleteContact(contact.id) }
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
private fun SwipeToDeleteContactItem(
    contact: Contact,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isRemoved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showDeleteDialog = true
                false
            } else {
                false
            }
        }
    )

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete Contact",
            message = "Are you sure you want to delete this message from \"${contact.name}\"?",
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
            ContactCard(
                contact = contact,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun ContactCard(
    contact: Contact,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scaleOnPress()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!contact.isRead) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Mail icon with unread indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (!contact.isRead) {
                            Pink500.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (!contact.isRead) Icons.Outlined.Mail else Icons.Outlined.MarkEmailRead,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (!contact.isRead) Pink500 else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (!contact.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (!contact.isRead) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Pink500)
                        )
                    }
                }

                Text(
                    text = contact.subject,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (!contact.isRead) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = contact.message,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = contact.email,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Text(
                        text = contact.submittedAt.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
